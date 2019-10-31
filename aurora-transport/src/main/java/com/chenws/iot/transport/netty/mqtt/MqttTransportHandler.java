package com.chenws.iot.transport.netty.mqtt;

import com.chenws.iot.transport.netty.mqtt.protocol.Process;
import com.chenws.iot.transport.netty.mqtt.session.MqttSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
public class MqttTransportHandler extends SimpleChannelInboundHandler<MqttMessage> implements GenericFutureListener<Future<? super Void>> {

    private Process process;

    MqttTransportHandler(Process process) {
        this.process = process;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        log.info("Accept msg: {}", msg);
        handleMqttMessage(ctx, msg);
    }

    private void handleMqttMessage(ChannelHandlerContext ctx, MqttMessage msg) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (msg.fixedHeader() == null) {
            log.info("[{}:{}] Invalid message received", address.getHostName(), address.getPort());
            process.getDisConnect().handleDisConnect(ctx.channel(), msg);
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
            case PUBREL:
                process.getPubRel().handlePubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
            case PUBCOMP:
                process.getPubComp().handlePubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
            case SUBSCRIBE:
                process.getSubscribe().handleSubscribe(ctx.channel(), (MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                process.getUnSubscribe().handleUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg);
                break;
            case PINGREQ:
                process.getPingReq().handlePingReq(ctx.channel(), msg);
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
        if (future.isSuccess()){
            log.info("future success");
        }else{
            log.info("future fail");
        }
    }

    /**
     * 长时间没接受或发送
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                Channel channel = ctx.channel();
                String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
                // 发送遗嘱消息
                if (process.getMqttSessionCache().containsKey(clientId)) {
                    MqttSession mqttSession = process.getMqttSessionCache().get(clientId);
                    if (mqttSession.getWillMessage() != null) {
                        process.getPublish().handlePublish(ctx.channel(), mqttSession.getWillMessage());
                    }
                }
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 中断连接
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException){
            Channel channel = ctx.channel();
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            // 发送遗嘱消息
            if (process.getMqttSessionCache().containsKey(clientId)) {
                MqttSession mqttSession = process.getMqttSessionCache().get(clientId);
                if (mqttSession.getWillMessage() != null) {
                    process.getPublish().handlePublish(ctx.channel(), mqttSession.getWillMessage());
                }
            }
            ctx.close();
        }else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
