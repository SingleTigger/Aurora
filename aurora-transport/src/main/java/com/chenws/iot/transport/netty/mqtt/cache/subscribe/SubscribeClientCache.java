package com.chenws.iot.transport.netty.mqtt.cache.subscribe;

import com.chenws.iot.common.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by chenws on 2019/10/22.
 */
@Component
public class SubscribeClientCache {

    @Autowired
    private RedisTemplate redisTemplate;

    public void putTopicFilter(String clientId,String topicFilter){
        redisTemplate.opsForSet().add(RedisConstant.CLIENT_TOPIC + clientId,topicFilter);
    }

    public void removeTopicFilter(String clientId,String topicFilter){
        redisTemplate.opsForSet().remove(clientId,topicFilter);
    }

    public Set<String> topicFilterByClientId(String clientId){
        return redisTemplate.opsForSet().members(RedisConstant.CLIENT_TOPIC + clientId);
    }

}
