package com.chenws.iot.transport.netty.mqtt;

import com.chenws.iot.transport.netty.mqtt.protocol.Process;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.mqtt.MqttMessageType.PINGRESP;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
public class MqttTransportHandler extends SimpleChannelInboundHandler<MqttMessage> implements GenericFutureListener<Future<? super Void>> {

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
//            process.processDisconnect(ctx);
            return;
        }
        switch (msg.fixedHeader().messageType()) {
            case CONNECT:
                process.getConnect().handleConnect(ctx.channel(), (MqttConnectMessage) msg);
                break;
            case PUBLISH:
                process.getPublish().handlePublish(ctx.channel(), (MqttPublishMessage) msg);
                break;
            case PUBACK:
                process.getPubAck().handlePubAck(ctx.channel(), (MqttPubAckMessage) msg);
                break;
            case PUBREC:
                process.getPubRec().handlePubRec(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
//            case PUBREL:
//                protocolProcess.pubRel().processPubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
//                break;
//            case PUBCOMP:
//                protocolProcess.pubComp().processPubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
//                break;
            case SUBSCRIBE:
                process.getSubscribe().handleSubscribe(ctx.channel(), (MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                process.getUnSubscribe().handleUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg);
                break;
            case PINGREQ:
                process.getPingReq().handlePingReq(ctx.channel(), msg);
                break;
            case PINGRESP:
                break;
            case DISCONNECT:
                process.getDisConnect().handleDisConnect(ctx.channel(), msg);
                break;
            default:
                break;
        }
    }

    @Override
    public void operationComplete(Future<? super Void> future) throws Exception {

    }
}
