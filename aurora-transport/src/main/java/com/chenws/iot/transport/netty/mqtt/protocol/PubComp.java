package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.service.DupPubRelMsgService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/10/11.
 */
@Component
@Slf4j
public class PubComp {

    @Autowired
    private DupPubRelMsgService dupPubRelMsgService;

    public void handlePubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.info("PUBCOMP - clientId: {}, messageId: {}", channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        dupPubRelMsgService.remove((String)channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
    }
}
