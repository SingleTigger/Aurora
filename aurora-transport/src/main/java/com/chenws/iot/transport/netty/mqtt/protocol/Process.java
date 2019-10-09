package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.session.MqttSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenws on 2019/10/8.
 */
@Component
public class Process {

    @Autowired
    private Connect connect;

    public void processDisconnect(ChannelHandlerContext ctx) {
    }

    public void processPublish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
    }

    public void processSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
    }

    public void processUnsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
    }

    public Connect getConnect() {
        return connect;
    }
}
