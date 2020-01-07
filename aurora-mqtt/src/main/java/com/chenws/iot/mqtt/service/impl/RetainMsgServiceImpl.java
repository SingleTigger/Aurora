package com.chenws.iot.mqtt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.chenws.iot.common.utils.RedisUtil;
import com.chenws.iot.mqtt.bean.RetainMessageBO;
import com.chenws.iot.mqtt.service.RetainMsgService;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.chenws.iot.common.constant.RedisConstant.RETAIN_MESSAGE;

/**
 * Created by chenws on 2019/10/11.
 */
@Service
public class RetainMsgServiceImpl implements RetainMsgService {

    private final RedisUtil redisUtil;

    public RetainMsgServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void put(String topic, RetainMessageBO retainMessageStore) {
        redisUtil.set(RETAIN_MESSAGE + topic, retainMessageStore);
    }

    @Override
    public RetainMessageBO get(String topic) {
        return (RetainMessageBO) redisUtil.get(RETAIN_MESSAGE + topic);
    }

    @Override
    public void remove(String topic) {
        redisUtil.delete(RETAIN_MESSAGE + topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return redisUtil.hasKey(RETAIN_MESSAGE + topic);
    }

    @Override
    public List<RetainMessageBO> search(String topicFilter) {
        List<RetainMessageBO> retainMessageStores = new ArrayList<>();
        if (!StrUtil.contains(topicFilter, '#') && !StrUtil.contains(topicFilter, '+')) {
            if (containsKey(topicFilter)) {
                retainMessageStores.add(get(topicFilter));
            }
        } else {
            Map<String, RetainMessageBO> map = new HashMap<>();
            Set<String> set = redisUtil.getKeys(RETAIN_MESSAGE + "*");
            if (set != null) {
                set.forEach(
                        entry -> {
                            map.put(entry.substring(RETAIN_MESSAGE.length()), (RetainMessageBO) redisUtil.get(entry));
                        }
                );
            }

            map.forEach((topic, retainMessageBO) -> {
                if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                    List<String> splitTopics = StrUtil.split(topic, '/');
                    List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');
                    StringBuilder newTopicFilter = new StringBuilder();
                    for (int i = 0; i < spliteTopicFilters.size(); i++) {
                        String value = spliteTopicFilters.get(i);
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
                        retainMessageStores.add(retainMessageBO);
                    }
                }
            });
        }
        return retainMessageStores;
    }
}
