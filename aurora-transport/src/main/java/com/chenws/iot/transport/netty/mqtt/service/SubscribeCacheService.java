package com.chenws.iot.transport.netty.mqtt.service;

import com.chenws.iot.transport.netty.mqtt.bean.SubscribeBO;

/**
 * Created by chenws on 2019/10/9.
 */
public interface SubscribeCacheService {

    /**
     * 保存订阅
     * @param topicFilter
     * @param subscribeBO
     */
    void put(String topicFilter, SubscribeBO subscribeBO);

    void remove();

}
