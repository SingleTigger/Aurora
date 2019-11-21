package com.chenws.iot.analyze.kafka;

import com.chenws.iot.analyze.handle.PublishMsgHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.chenws.iot.common.constant.KafkaConstant.PUBLISH_TOPIC;

/**
 * Created by chenws on 2019/11/18.
 */
@Slf4j
@Component
public class KafkaPublishReceiver {

    @Autowired
    private PublishMsgHandler publishMsgHandler;

    @KafkaListener(topics = {PUBLISH_TOPIC})
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            byte[] message = (byte[]) kafkaMessage.get();
            publishMsgHandler.handlerMsg(message);
        }
    }
}
