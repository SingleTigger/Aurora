package com.chenws.iot.transport.netty.mqtt.bean;

import com.chenws.iot.transport.netty.mqtt.topic.Topic;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenws on 2019/10/9.
 */
@Data
@NoArgsConstructor
public class SubscribeBO {

    private String clientId;

    private Topic topicFilter;

    private int mqttQoS;

    public SubscribeBO(String clientId, Topic topicFilter, int mqttQoS) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.mqttQoS = mqttQoS;
    }

    public SubscribeBO(SubscribeBO subscribeBO) {
        this.mqttQoS = subscribeBO.mqttQoS;
        this.clientId = subscribeBO.clientId;
        this.topicFilter = subscribeBO.topicFilter;
    }
}
