package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.session.MqttSessionCache;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/10/8.
 */
@Component
@Getter
public class Process {

    @Autowired
    private MqttSessionCache mqttSessionCache;

    @Autowired
    private Connect connect;

    @Autowired
    private DisConnect disConnect;

    @Autowired
    private Publish publish;

    @Autowired
    private Subscribe subscribe;

    @Autowired
    private PubAck pubAck;

    @Autowired
    private PubComp pubComp;

    @Autowired
    private PubRec pubRec;

    @Autowired
    private PubRel pubRel;

    @Autowired
    private UnSubscribe unSubscribe;

    @Autowired
    private PingReq pingReq;

}
