package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.bean.DupPublishMessageBO;
import com.chenws.iot.transport.netty.mqtt.bean.RetainMessageBO;
import com.chenws.iot.transport.netty.mqtt.bean.SubscribeBO;
import com.chenws.iot.transport.netty.mqtt.service.DupPublishMsgService;
import com.chenws.iot.transport.netty.mqtt.service.PacketIdService;
import com.chenws.iot.transport.netty.mqtt.service.RetainMsgService;
import com.chenws.iot.transport.netty.mqtt.service.SubscribeService;
import com.chenws.iot.transport.netty.mqtt.session.MqttSessionCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by chenws on 2019/10/10.
 */
@Component
@Slf4j
public class Publish {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private MqttSessionCache mqttSessionCache;

    @Autowired
    private DupPublishMsgService dupPublishMsgService;

    @Autowired
    private RetainMsgService retainMsgService;

    @Autowired
    private PacketIdService packetIdService;


    public void handlePublish(Channel channel, MqttPublishMessage msg) {
        MqttQoS mqttQoS = msg.fixedHeader().qosLevel();
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false);
        if(mqttQoS == MqttQoS.AT_LEAST_ONCE){
            sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        else if(mqttQoS == MqttQoS.EXACTLY_ONCE){
            sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                retainMsgService.remove(msg.variableHeader().topicName());
            } else {
                RetainMessageBO retainMessageBO = new RetainMessageBO(msg.variableHeader().topicName(),messageBytes,msg.fixedHeader().qosLevel().value());
                retainMsgService.put(msg.variableHeader().topicName(), retainMessageBO);
            }
        }
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain) {
        List<SubscribeBO> subscribeBOS = subscribeService.search(topic);
        for (SubscribeBO subscribeBO : subscribeBOS) {
            if (mqttSessionCache.containsKey(subscribeBO.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS，取小的
                MqttQoS respQoS = mqttQoS.value() > subscribeBO.getMqttQoS() ? MqttQoS.valueOf(subscribeBO.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = build(respQoS,retain,topic,0,messageBytes);
                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeBO.getClientId(), topic, respQoS.value());
                    mqttSessionCache.get(subscribeBO.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE || respQoS == MqttQoS.EXACTLY_ONCE) {
                    Integer packetId = packetIdService.getPacketId();
                    MqttPublishMessage publishMessage = build(respQoS,retain,topic,packetId,messageBytes);
                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, packetId: {}", subscribeBO.getClientId(), topic, respQoS.value(), packetId);
                    DupPublishMessageBO dupPublishMessageBO = new DupPublishMessageBO(subscribeBO.getClientId(),topic,respQoS.value(),packetId,messageBytes);
                    dupPublishMsgService.put(subscribeBO.getClientId(), dupPublishMessageBO);
                    mqttSessionCache.get(subscribeBO.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        }
    }

    private MqttPublishMessage build(MqttQoS mqttQoS,boolean retain,String topic,int packetId,byte[] messageBytes){
        return (MqttPublishMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBLISH, false, mqttQoS, retain, 0),
                new MqttPublishVariableHeader(topic, packetId), Unpooled.buffer().writeBytes(messageBytes));
    }

    private void sendPubAckMessage(Channel channel, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(Channel channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
        channel.writeAndFlush(pubRecMessage);
    }
}
