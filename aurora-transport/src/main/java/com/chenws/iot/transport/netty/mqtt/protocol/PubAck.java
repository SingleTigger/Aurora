package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.service.DupPublishMsgService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/10/11.
 */
@Component
@Slf4j
public class PubAck {

    @Autowired
    private DupPublishMsgService dupPublishMsgService;

    public void handlePubAck(Channel channel, MqttPubAckMessage msg) {
        int messageId = msg.variableHeader().messageId();
        log.info("PUBACK - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        dupPublishMsgService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
    }
}
