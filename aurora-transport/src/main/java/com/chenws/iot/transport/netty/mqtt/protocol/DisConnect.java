package com.chenws.iot.transport.netty.mqtt.protocol;

import com.chenws.iot.transport.netty.mqtt.service.DupPubRelMsgService;
import com.chenws.iot.transport.netty.mqtt.service.DupPublishMsgService;
import com.chenws.iot.transport.netty.mqtt.service.SubscribeService;
import com.chenws.iot.transport.netty.mqtt.session.MqttSession;
import com.chenws.iot.transport.netty.mqtt.session.MqttSessionCache;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/10/10.
 */
@Component
@Slf4j
public class DisConnect {

    @Autowired
    private MqttSessionCache mqttSessionCache;

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private DupPubRelMsgService dupPubRelMsgService;

    @Autowired
    private DupPublishMsgService dupPublishMsgService;

    public void handleDisConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        MqttSession mqttSession = mqttSessionCache.get(clientId);
        if (mqttSession != null && mqttSession.isCleanSession()) {
            //清除相关的主题订阅
            subscribeService.removeByClient(clientId);
            dupPublishMsgService.removeByClient(clientId);
            dupPubRelMsgService.removeByClient(clientId);
            log.info("DISCONNECT - clientId: {}, cleanSession: {}", clientId, mqttSession.isCleanSession());
            mqttSessionCache.remove(clientId);
        }
        channel.close();
    }
}
