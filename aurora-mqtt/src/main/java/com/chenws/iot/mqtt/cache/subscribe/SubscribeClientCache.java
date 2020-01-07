package com.chenws.iot.mqtt.cache.subscribe;

import com.chenws.iot.common.constant.RedisConstant;
import com.chenws.iot.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by chenws on 2019/10/22.
 */
@Component
public class SubscribeClientCache {

    private final RedisUtil redisUtil;

    public SubscribeClientCache(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void putTopicFilter(String clientId,String topicFilter){
        redisUtil.sAdd(RedisConstant.CLIENT_TOPIC + clientId,topicFilter);
    }

    public void removeTopicFilter(String clientId,String topicFilter){
        redisUtil.sRemove(RedisConstant.CLIENT_TOPIC + clientId,topicFilter);
    }

    public Set<Object> topicFilterByClientId(String clientId){
        return redisUtil.sGet(RedisConstant.CLIENT_TOPIC + clientId);
    }

}
