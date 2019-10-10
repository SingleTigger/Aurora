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
public class SubscribeNotWildcardCache {

    @Autowired
    private RedisTemplate redisTemplate;

    public SubscribeBO put(String topic,String clientId,SubscribeBO subscribeStore){
        redisTemplate.opsForHash().put(RedisConstant.NOT_WILDCARD_TOPIC + topic,clientId,subscribeStore);
        redisTemplate.opsForSet().add(RedisConstant.CLIENT_TOPIC + clientId,topic);
        return subscribeStore;
    }
    public SubscribeBO get(String topic,String clientId){
        return (SubscribeBO) redisTemplate.opsForHash().get(RedisConstant.NOT_WILDCARD_TOPIC + topic,clientId);
    }
    public boolean containsKey(String topic,String clientId){
        return redisTemplate.opsForHash().hasKey(RedisConstant.NOT_WILDCARD_TOPIC + topic,clientId);
    }
    public void remove(String topic,String clientId){
        redisTemplate.opsForSet().remove(RedisConstant.CLIENT_TOPIC + clientId,topic);
        redisTemplate.opsForHash().delete(RedisConstant.NOT_WILDCARD_TOPIC + topic,clientId);
    }
    public void removeByClient(String clientId){
        Set<String> topicFilters = redisTemplate.opsForSet().members(RedisConstant.CLIENT_TOPIC + clientId);
        for (String topicFilter : topicFilters){
            redisTemplate.opsForHash().delete(RedisConstant.NOT_WILDCARD_TOPIC + topicFilter,clientId);
        }
        redisTemplate.delete(RedisConstant.CLIENT_TOPIC + clientId);
    }
    public Map<String, ConcurrentHashMap<String, SubscribeBO>> all(){
        Map<String, ConcurrentHashMap<String, SubscribeBO>> map = new HashMap<>();
        Set<String> set = redisTemplate.keys(RedisConstant.NOT_WILDCARD_TOPIC + "*");
        if (set != null && !set.isEmpty()) {
            set.forEach(
                    entry -> {
                        ConcurrentHashMap<String, SubscribeBO> map1 = new ConcurrentHashMap<>();
                        Map<Object, Object> map2 = redisTemplate.opsForHash().entries(entry);
                        if (map2 != null && !map2.isEmpty()) {
                            map2.forEach((k, v) -> {
                                map1.put((String)k, (SubscribeBO)v);
                            });
                            map.put(entry.substring(RedisConstant.NOT_WILDCARD_TOPIC.length()), map1);
                        }
                    }
            );
        }
        return map;
    }
    public List<SubscribeBO> all(String topic){
        List<SubscribeBO> list = new ArrayList<>();
        Map<String,SubscribeBO> map = redisTemplate.opsForHash().entries(RedisConstant.NOT_WILDCARD_TOPIC + topic);
        if (map != null) {
            map.forEach((k, v) -> {
                list.add(v);
            });
        }
        return list;
    }



}
