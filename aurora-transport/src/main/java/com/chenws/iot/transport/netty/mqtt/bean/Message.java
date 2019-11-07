package com.chenws.iot.transport.netty.mqtt.bean;

import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.Data;

import java.util.Map;

@Data
public class Message {

    private int msgId;

    private int qos;

    private String topic;

    private Map<String,Object> headers;

    private String clientId;

    private MqttMessageType type;

    private byte[] payload;

}
