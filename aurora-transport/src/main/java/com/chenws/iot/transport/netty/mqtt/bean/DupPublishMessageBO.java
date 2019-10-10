package com.chenws.iot.transport.netty.mqtt.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenws on 2019/10/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DupPublishMessageBO{

    private String clientId;

    private String topicFilter;

    private int mqttQoS;

    private int messageId;

    private byte[] messageBytes;

}
