package com.chenws.iot.transport.netty.mqtt;

import com.chenws.iot.transport.netty.mqtt.session.MqttSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.mqtt.MqttMessageType.PINGRESP;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
public class MqttTransportHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private ConcurrentHashMap<String,Boolean> connectStatus = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, MqttSession> sessions = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        log.info("Accept msg: {}", msg);
        handleMqttMessage(ctx,msg);
    }

    private void handleMqttMessage(ChannelHandlerContext ctx, MqttMessage msg) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (msg.fixedHeader() == null) {
            log.info("[{}:{}] Invalid message received", address.getHostName(), address.getPort());
            processDisconnect(ctx);
            return;
        }
        switch (msg.fixedHeader().messageType()) {
            case CONNECT:
                handleConnect(ctx, (MqttConnectMessage) msg);
                break;
            case PUBLISH:
                processPublish(ctx, (MqttPublishMessage) msg);
                break;
            case SUBSCRIBE:
                processSubscribe(ctx, (MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                processUnsubscribe(ctx, (MqttUnsubscribeMessage) msg);
                break;
            case PINGREQ:
                if (checkConnected(ctx, msg)) {
                    ctx.writeAndFlush(new MqttMessage(new MqttFixedHeader(PINGRESP, false, AT_MOST_ONCE, false, 0)));
                    transportService.reportActivity(sessionInfo);
                    if (gatewaySessionHandler != null) {
                        gatewaySessionHandler.reportActivity();
                    }
                }
                break;
            case DISCONNECT:
                if (checkConnected(ctx, msg)) {
                    processDisconnect(ctx);
                }
                break;
            default:
                break;
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
                ctx.writeAndFlush(connAckMessage);
                ctx.close();
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
                ctx.writeAndFlush(connAckMessage);
                ctx.close();
                return;
            }
            ctx.close();
            return;
        }
        String clientIdentifier = msg.payload().clientIdentifier();
        if (StringUtils.isBlank(clientIdentifier)) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            ctx.writeAndFlush(connAckMessage);
            ctx.close();
            return;
        }
        Boolean status = connectStatus.get(clientIdentifier);
        if(status){
            MqttSession mqttSession = sessions.get(clientIdentifier);
            Optional.ofNullable(mqttSession).orElseGet(() -> replyConnAckMessage(ctx,MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE));
        }
        connectStatus.put(clientIdentifier,Boolean.TRUE);


    }

    private void replyConnAckMessage(ChannelHandlerContext ctx,MqttConnectReturnCode mqttConnectReturnCode){
        MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(mqttConnectReturnCode, false), null);
        ctx.writeAndFlush(connAckMessage);
        ctx.close();
    }
}
