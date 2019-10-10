package com.chenws.iot.transport.netty.mqtt.cache.subscribe;

import com.chenws.iot.common.constant.RedisConstant;
import com.chenws.iot.transport.netty.mqtt.bean.SubscribeBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenws on 2019/10/10.
 */
@Component
public class SubscribeWildcardCache {

    @Autowired
    private RedisTemplate redisTemplate;

    public SubscribeBO put(String topicFilter,String clientId,SubscribeBO subscribeBO){
        redisTemplate.opsForHash().put(RedisConstant.WILDCARD_TOPIC + topicFilter,clientId,subscribeBO);
        redisTemplate.opsForSet().add(RedisConstant.CLIENT_TOPIC + clientId,topicFilter);
        return subscribeBO;
    }

    public SubscribeBO get(String topicFilter,String clientId){
        return (SubscribeBO) redisTemplate.opsForHash().get(RedisConstant.WILDCARD_TOPIC + topicFilter,clientId);
    }

    public boolean containsKey(String topicFilter, String clientId) {
        return redisTemplate.opsForHash().hasKey(RedisConstant.WILDCARD_TOPIC + topicFilter, clientId);
    }

    public void remove(String topicFilter, String clientId) {
        redisTemplate.opsForSet().remove(RedisConstant.CLIENT_TOPIC + clientId, topicFilter);
        redisTemplate.opsForHash().delete(RedisConstant.WILDCARD_TOPIC + topicFilter, clientId);
    }


    public void removeByClient(String clientId) {
        Set<String> topicFilters = redisTemplate.opsForSet().members(RedisConstant.CLIENT_TOPIC + clientId);
        if(topicFilters == null)
            return;
        for (String topicFilter : topicFilters){
            redisTemplate.opsForHash().delete(RedisConstant.WILDCARD_TOPIC + topicFilter, clientId);
        }
        redisTemplate.delete(RedisConstant.CLIENT_TOPIC + clientId);
    }

    public Map<String, ConcurrentHashMap<String, SubscribeBO>> all(){
        Map<String, ConcurrentHashMap<String, SubscribeBO>> result = new HashMap<>();
        Set<String> keys = redisTemplate.keys( RedisConstant.WILDCARD_TOPIC + "*");
        if (keys != null) {
            keys.forEach(
                    key -> {
                        ConcurrentHashMap<String, SubscribeBO> concurrentHashMap = new ConcurrentHashMap<>();
                        Map<String, SubscribeBO> map = redisTemplate.opsForHash().entries(key);
                        if (map != null ) {
                            map.forEach(concurrentHashMap::put);
                            result.put(key.substring(RedisConstant.WILDCARD_TOPIC.length()), concurrentHashMap);
                        }
                    }
            );
        }
        return result;
    }
    public List<SubscribeBO> all(String topic){
        List<SubscribeBO> list = new ArrayList<>();
        Map<Object,Object> map = redisTemplate.opsForHash().entries(RedisConstant.WILDCARD_TOPIC + topic);
        if (map != null && !map.isEmpty()) {
            map.forEach((k, v) -> {
                list.add((SubscribeBO) v);
            });
        }
        return list;
    }



}
