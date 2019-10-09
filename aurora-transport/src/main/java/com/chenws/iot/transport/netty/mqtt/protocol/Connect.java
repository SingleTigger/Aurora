package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.session.MqttSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenws on 2019/10/9.
 */
@Component
public class Connect {

    private ConcurrentHashMap<String, MqttSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private

    public void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                replyConnAckMessage(ctx, MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                replyConnAckMessage(ctx,MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
                return;
            }
            ctx.close();
            return;
        }
        String clientIdentifier = msg.payload().clientIdentifier();
        if (StringUtils.isBlank(clientIdentifier)) {
            replyConnAckMessage(ctx,MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            return;
        }
        if(sessions.containsKey(clientIdentifier)){
            //把之前的session关闭
            MqttSession mqttSession = sessions.get(clientIdentifier);
            boolean cleanSession = mqttSession.isCleanSession();
            if(cleanSession){
                sessions.remove(clientIdentifier);

            }
            mqttSession.getChannel().close();


        }
    }

    private void replyConnAckMessage(ChannelHandlerContext ctx,MqttConnectReturnCode mqttConnectReturnCode){
        MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(mqttConnectReturnCode, false), null);
        ctx.writeAndFlush(connAckMessage);
        ctx.close();
    }
}
