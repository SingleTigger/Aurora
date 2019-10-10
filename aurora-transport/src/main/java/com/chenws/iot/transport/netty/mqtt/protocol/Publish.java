package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.bean.DupPublishMessageBO;
import com.chenws.iot.transport.netty.mqtt.bean.SubscribeBO;
import com.chenws.iot.transport.netty.mqtt.service.DupPubRelMsgService;
import com.chenws.iot.transport.netty.mqtt.service.DupPublishMsgService;
import com.chenws.iot.transport.netty.mqtt.service.SubscribeService;
import com.chenws.iot.transport.netty.mqtt.session.MqttSessionCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DupPubRelMsgService dupPubRelMsgService;


    public void handlePublish(Channel channel, MqttPublishMessage msg) {
        //得到用户id
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        MqttQoS mqttQoS = msg.fixedHeader().qosLevel();
        int packetId = msg.variableHeader().packetId();
        if(mqttQoS == MqttQoS.AT_MOST_ONCE){
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        }
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup,int packetId) {
        List<SubscribeBO> subscribeBOS = subscribeService.search(topic);
        for (SubscribeBO subscribeBO : subscribeBOS) {
            if (mqttSessionCache.containsKey(subscribeBO.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeBO.getMqttQoS() ? MqttQoS.valueOf(subscribeBO.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = build(dup,respQoS,retain,topic,packetId,messageBytes);
                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeBO.getClientId(), topic, respQoS.value());
                    mqttSessionCache.get(subscribeBO.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    MqttPublishMessage publishMessage = build(dup,respQoS,retain,topic,packetId,messageBytes);
                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, packetId: {}", subscribeBO.getClientId(), topic, respQoS.value(), packetId);
                    DupPublishMessageBO dupPublishMessageBO = new DupPublishMessageBO(subscribeBO.getClientId(),topic,respQoS.value(),packetId,messageBytes);
                    dupPublishMsgService.put(subscribeBO.getClientId(), dupPublishMessageBO);
                    mqttSessionCache.get(subscribeBO.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    MqttPublishMessage publishMessage = build(dup,respQoS,retain,topic,packetId,messageBytes);
                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, packetId: {}", subscribeBO.getClientId(), topic, respQoS.value(), packetId);
                    DupPublishMessageBO dupPublishMessageBO = new DupPublishMessageBO(subscribeBO.getClientId(),topic,respQoS.value(),packetId,messageBytes);
                    dupPublishMsgService.put(subscribeBO.getClientId(), dupPublishMessageBO);
                    mqttSessionCache.get(subscribeBO.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        }
    }

    private MqttPublishMessage build(boolean dup,MqttQoS mqttQoS,boolean retain,String topic,int packetId,byte[] messageBytes){
        return (MqttPublishMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBLISH, dup, mqttQoS, retain, 0),
                new MqttPublishVariableHeader(topic, packetId), Unpooled.buffer().writeBytes(messageBytes));
    }
}
