package com.chenws.iot.transport.netty.mqtt;

import com.chenws.iot.transport.netty.mqtt.protocol.Process;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.mqtt.MqttMessageType.PINGRESP;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
public class MqttTransportHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private Process process;

    public MqttTransportHandler(Process process){
        this.process = process;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        log.info("Accept msg: {}", msg);
        handleMqttMessage(ctx,msg);
    }

    private void handleMqttMessage(ChannelHandlerContext ctx, MqttMessage msg) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (msg.fixedHeader() == null) {
            log.info("[{}:{}] Invalid message received", address.getHostName(), address.getPort());
            process.processDisconnect(ctx);
            return;
        }
        switch (msg.fixedHeader().messageType()) {
            case CONNECT:
                process.getConnect().handleConnect(ctx, (MqttConnectMessage) msg);
                break;
            case PUBLISH:
                process.processPublish(ctx, (MqttPublishMessage) msg);
                break;
            case SUBSCRIBE:
                process.processSubscribe(ctx, (MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                process.processUnsubscribe(ctx, (MqttUnsubscribeMessage) msg);
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


}
