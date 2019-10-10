package com.chenws.iot.transport.netty.mqtt.protocol;

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
    private Connect connect;

    @Autowired
    private DisConnect disConnect;

    @Autowired
    private Publish publish;

}
