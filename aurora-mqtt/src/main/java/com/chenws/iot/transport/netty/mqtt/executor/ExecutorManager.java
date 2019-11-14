package com.chenws.iot.transport.netty.mqtt.executor;

import com.chenws.iot.common.executor.CustomThreadFactory;
import com.chenws.iot.common.executor.RejectHandler;
import com.chenws.iot.transport.netty.mqtt.constants.MqttTypeConstant;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.chenws.iot.common.constant.JvmCoreThreadConstant.coreThreadNum;

/**
 * Created by chenws on 2019/10/31.
 */
@Component
public final class ExecutorManager {

    private final Map<String,ExecutorService> executorMap = new HashMap<>(4);

    ExecutorManager(){
        init();
    }

    public ExecutorService getExecutor(String mqttType){
        return executorMap.get(mqttType);
    }

    private void init(){
        ExecutorService connect = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new CustomThreadFactory("connectThread"),
                new RejectHandler("connect", 100000));

        ExecutorService publish = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new CustomThreadFactory("publishThread"),
                new RejectHandler("publish", 100000));

        ExecutorService subscribe = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new CustomThreadFactory("subscribeThread"),
                new RejectHandler("subscribe", 100000));

        ExecutorService ping = new ThreadPoolExecutor(coreThreadNum,
                coreThreadNum,
                60000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new CustomThreadFactory("pingThread"),
                new RejectHandler("ping", 100000));

        executorMap.put(MqttTypeConstant.CONNECT,connect);
        executorMap.put(MqttTypeConstant.DISCONNECT,connect);
        executorMap.put(MqttTypeConstant.PING_REQ,ping);
        executorMap.put(MqttTypeConstant.PUBLISH,publish);
        executorMap.put(MqttTypeConstant.PUB_ACK,publish);
        executorMap.put(MqttTypeConstant.PUB_REL,publish);
        executorMap.put(MqttTypeConstant.SUBSCRIBE,subscribe);
        executorMap.put(MqttTypeConstant.UNSUBSCRIBE,subscribe);
        executorMap.put(MqttTypeConstant.PUB_REC,subscribe);
        executorMap.put(MqttTypeConstant.PUB_COMP,subscribe);

    }

}
