package com.chenws.iot.transport.netty.mqtt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.chenws.iot.transport.netty.mqtt.bean.SubscribeBO;
import com.chenws.iot.transport.netty.mqtt.cache.subscribe.SubscribeNotWildcardCache;
import com.chenws.iot.transport.netty.mqtt.cache.subscribe.SubscribeWildcardCache;
import com.chenws.iot.transport.netty.mqtt.service.SubscribeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by chenws on 2019/10/10.
 */
@Service
public class SubscribeServiceImpl implements SubscribeService {

    @Autowired
    private SubscribeNotWildcardCache subscribeNotWildcardCache;;

    @Autowired
    private SubscribeWildcardCache subscribeWildcardCache;

    @Override
    public void put(String topicFilter, SubscribeBO subscribeBO) {
        if (StringUtils.contains(topicFilter,'#') || StringUtils.contains(topicFilter,'+')){
            subscribeWildcardCache.put(topicFilter,subscribeBO.getClientId(),subscribeBO);
        }else{
            subscribeNotWildcardCache.put(topicFilter,subscribeBO.getClientId(),subscribeBO);
        }
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StringUtils.contains(topicFilter,'#') || StringUtils.contains(topicFilter,'+')){
            subscribeWildcardCache.remove(topicFilter,clientId);
        }else {
            subscribeNotWildcardCache.remove(topicFilter,clientId);
        }
    }

    @Override
    public void removeByClient(String clientId) {
        subscribeNotWildcardCache.removeByClient(clientId);
        subscribeWildcardCache.removeByClient(clientId);
    }

    @Override
    public List<SubscribeBO> search(String topic) {
        List<SubscribeBO> subscribeStores = new ArrayList<>();
        List<SubscribeBO> notWildcardTopics = subscribeNotWildcardCache.all(topic);
        if (notWildcardTopics.size() > 0) {
            subscribeStores.addAll(notWildcardTopics);
        }
        subscribeWildcardCache.all().forEach((topicFilter, map) -> {
            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                List<String> splitTopics = Arrays.asList(StringUtils.split(topic, '/'));//a
                List<String> splitTopicFilters = Arrays.asList(StringUtils.split(topicFilter, '/'));//#
                StringBuilder newTopicFilter = new StringBuilder();
                for (int i = 0; i < splitTopicFilters.size(); i++) {
                    String value = splitTopicFilters.get(i);
                    if (value.equals("+")) {
                        newTopicFilter.append("+/");
                    } else if (value.equals("#")) {
                        newTopicFilter.append("#/");
                        break;
                    } else {
                        newTopicFilter.append(splitTopics.get(i)).append("/");
                    }
                }
                newTopicFilter = new StringBuilder(StrUtil.removeSuffix(newTopicFilter.toString(), "/"));
                if (topicFilter.equals(newTopicFilter.toString())) {
                    Collection<SubscribeBO> collection = map.values();
                    List<SubscribeBO> list2 = new ArrayList<>(collection);
                    subscribeStores.addAll(list2);
                }
            }
        });
        return subscribeStores;
    }

}
