package com.chenws.iot.mqtt.protocol;

import com.chenws.iot.mqtt.service.DupPubRelMsgService;
import com.chenws.iot.mqtt.bean.DupPubRelMessageBO;
import com.chenws.iot.mqtt.service.DupPublishMsgService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/10/11.
 */
@Component
@Slf4j
public class PubRec {

    private final DupPublishMsgService dupPublishMsgService;

    private final DupPubRelMsgService dupPubRelMsgService;

    public PubRec(DupPublishMsgService dupPublishMsgService, DupPubRelMsgService dupPubRelMsgService) {
        this.dupPublishMsgService = dupPublishMsgService;
        this.dupPubRelMsgService = dupPubRelMsgService;
    }

    public void handlePubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()),
                null);
        log.info("PUBREC - clientId: {}, messageId: {}", channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        dupPublishMsgService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        DupPubRelMessageBO dupPubRelMessageStore = new DupPubRelMessageBO((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        dupPubRelMsgService.put((String) channel.attr(AttributeKey.valueOf("clientId")).get(), dupPubRelMessageStore);
        channel.writeAndFlush(pubRelMessage);
    }
}
