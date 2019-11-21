package com.chenws.iot.mqtt.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/11/13.
 */
@Slf4j
@Component
public class KafkaPublishSender {

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public void send(String topic,byte[] message) {
        if(message == null)
            return;
        kafkaTemplate.send(topic,message);
    }

}
