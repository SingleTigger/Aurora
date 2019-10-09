package com.chenws.iot.transport.netty.mqtt.bean;

import lombok.Data;

/**
 * Created by chenws on 2019/10/9.
 */
@Data
public class SubscribeBO {

    private String clientId;

    private String topicFilter;

    private int mqttQoS;

    public SubscribeBO(String clientId, String topicFilter, int mqttQoS) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.mqttQoS = mqttQoS;
    }
}
