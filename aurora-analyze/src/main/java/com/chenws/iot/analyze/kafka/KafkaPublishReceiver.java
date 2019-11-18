package com.chenws.iot.analyze.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

    @KafkaListener(topics = {PUBLISH_TOPIC})
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            String data = (String) kafkaMessage.get();
            log.info("----------------- message =" + data);
        }
    }
}
