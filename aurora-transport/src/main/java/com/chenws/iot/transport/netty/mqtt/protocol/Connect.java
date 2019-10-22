package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.bean.DupPubRelMessageBO;
import com.chenws.iot.transport.netty.mqtt.bean.DupPublishMessageBO;
import com.chenws.iot.transport.netty.mqtt.service.DupPubRelMsgService;
import com.chenws.iot.transport.netty.mqtt.service.DupPublishMsgService;
import com.chenws.iot.transport.netty.mqtt.service.SubscribeService;
import com.chenws.iot.transport.netty.mqtt.session.MqttSession;
import com.chenws.iot.transport.netty.mqtt.session.MqttSessionCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by chenws on 2019/10/9.
 */
@Component
public class Connect {

    @Autowired
    private MqttSessionCache mqttSessionCache;

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private DupPublishMsgService dupPublishMsgService;

    @Autowired
    private DupPubRelMsgService dupPubRelMsgService;

    public void handleConnect(Channel channel, MqttConnectMessage msg) {
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                replyConnAckMessage(channel, MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                replyConnAckMessage(channel,MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
                return;
            }
            channel.close();
            return;
        }
        String clientIdentifier = msg.payload().clientIdentifier();
        if (StringUtils.isBlank(clientIdentifier)) {
            replyConnAckMessage(channel,MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            return;
        }
        //TODO valid username & password
        if(mqttSessionCache.containsKey(clientIdentifier)){
            MqttSession mqttSession = mqttSessionCache.get(clientIdentifier);
            boolean cleanSession = mqttSession.isCleanSession();
            if(cleanSession){
                //清除session
                mqttSessionCache.remove(clientIdentifier);
                //清除重发publish消息
                dupPublishMsgService.removeByClient(clientIdentifier);
                //清除重发pubrel消息
                dupPubRelMsgService.removeByClient(clientIdentifier);
            }
            //把之前的session关闭
            mqttSession.getChannel().close();
        }

        //处理遗嘱信息
        MqttSession mqttSession = new MqttSession(msg.payload().clientIdentifier(), channel, msg.variableHeader().isCleanSession(), null);
        if (msg.variableHeader().isWillFlag()){
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH,false, MqttQoS.valueOf(msg.variableHeader().willQos()),msg.variableHeader().isWillRetain(),0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(),0),
                    Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes())
            );
            mqttSession.setWillMessage(willMessage);
        }
        //处理连接心跳包
        if (msg.variableHeader().keepAliveTimeSeconds() > 0){
            channel.pipeline().addFirst("idle",new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }
        mqttSessionCache.put(msg.payload().clientIdentifier(),mqttSession);
        channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        MqttConnAckMessage mqttConnAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK,false,MqttQoS.AT_MOST_ONCE,false,0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED,!msg.variableHeader().isCleanSession()),
                null
        );
        channel.writeAndFlush(mqttConnAckMessage);

        // 如果cleanSession为0, 需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
        if (!msg.variableHeader().isCleanSession()){
            List<DupPublishMessageBO> dupPublishMessageBOS = dupPublishMsgService.get(msg.payload().clientIdentifier());
            List<DupPubRelMessageBO> dupPubRelMessageBOS = dupPubRelMsgService.get(msg.payload().clientIdentifier());
            dupPublishMessageBOS.forEach(dupPublishMessageBO -> {
                MqttPublishMessage publishMessage = (MqttPublishMessage)MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,true,MqttQoS.valueOf(dupPublishMessageBO.getMqttQoS()),false,0),
                        new MqttPublishVariableHeader(dupPublishMessageBO.getTopicFilter(),dupPublishMessageBO.getMessageId()),
                        Unpooled.buffer().writeBytes(dupPublishMessageBO.getMessageBytes())
                );
                channel.writeAndFlush(publishMessage);
            });
            dupPubRelMessageBOS.forEach(dupPubRelMessageBO -> {
                MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL,true,MqttQoS.AT_MOST_ONCE,false,0),
                        MqttMessageIdVariableHeader.from(dupPubRelMessageBO.getMessageId()),
                        null
                );
                channel.writeAndFlush(pubRelMessage);
            });
        }
    }

    private void replyConnAckMessage(Channel channel,MqttConnectReturnCode mqttConnectReturnCode){
        MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(mqttConnectReturnCode, false), null);
        channel.writeAndFlush(connAckMessage);
        channel.close();
    }
}
