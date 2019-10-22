package com.chenws.iot.transport.netty.mqtt.protocol;

import cn.hutool.core.util.StrUtil;
import com.chenws.iot.transport.netty.mqtt.bean.RetainMessageBO;
import com.chenws.iot.transport.netty.mqtt.bean.SubscribeBO;
import com.chenws.iot.transport.netty.mqtt.service.RetainMsgService;
import com.chenws.iot.transport.netty.mqtt.service.SubscribeService;
import com.chenws.iot.transport.netty.mqtt.topic.Topic;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenws on 2019/10/11.
 */
@Component
@Slf4j
public class Subscribe {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private RetainMsgService retainMsgService;


    public void handleSubscribe(Channel channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        if (this.validTopicFilter(topicSubscriptions)) {
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            List<Integer> mqttQoSList = new ArrayList<>();
            for(MqttTopicSubscription mqttTopicSubscription : topicSubscriptions){
                String topicFilter = mqttTopicSubscription.topicName();
                MqttQoS mqttQoS = mqttTopicSubscription.qualityOfService();
                MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.SUBACK, false, mqttQoS, false, 0),
                        MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                        new MqttSubAckPayload(mqttQoSList));
                channel.writeAndFlush(subAckMessage);
                this.sendRetainMessage(channel, topicFilter, mqttQoS);

                Topic topic = new Topic(topicFilter);
                SubscribeBO subscribeBO = new SubscribeBO(clientId, topic, mqttQoS.value());
                subscribeService.put(topicFilter, subscribeBO);
                mqttQoSList.add(mqttQoS.value());
                log.info("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
            }
        } else {
            channel.close();
        }
    }
    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            // 以#或+符号开头的、以/符号结尾的订阅按非法订阅处理, 这里没有参考标准协议
            if (StrUtil.startWith(topicFilter, '+') || StrUtil.endWith(topicFilter, '/')) {
                return false;
            }
            if (StrUtil.contains(topicFilter, '#')) {
                // 如果出现多个#符号的订阅按非法订阅处理
                if (StrUtil.count(topicFilter, '#') > 1) {
                    return false;
                }
            }
            if (StrUtil.contains(topicFilter, '+')) {
                //如果+符号和/+字符串出现的次数不等的情况按非法订阅处理
                if (StrUtil.count(topicFilter, '+') != StrUtil.count(topicFilter, "/+")) {
                    return false;
                }

            }

        }
        return true;
    }

    private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS mqttQoS) {
        List<RetainMessageBO> retainMessageStores = retainMsgService.search(topicFilter);
        retainMessageStores.forEach(retainMessageStore -> {
            MqttQoS respQoS = retainMessageStore.getMqttQoS() > mqttQoS.value() ? mqttQoS : MqttQoS.valueOf(retainMessageStore.getMqttQoS());
            if (respQoS == MqttQoS.AT_MOST_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), 0), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
                log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessageStore.getTopic(), respQoS.value());
                channel.writeAndFlush(publishMessage);
            }
            if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), 1), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
                log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessageStore.getTopic(), respQoS.value(), 1);
                channel.writeAndFlush(publishMessage);
            }
            if (respQoS == MqttQoS.EXACTLY_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), 1), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
                log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessageStore.getTopic(), respQoS.value(), 1);
                channel.writeAndFlush(publishMessage);
            }
        });
    }
}
