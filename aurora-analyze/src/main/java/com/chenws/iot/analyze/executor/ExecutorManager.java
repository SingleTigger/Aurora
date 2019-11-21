package com.chenws.iot.analyze.executor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenws on 2019/10/31.
 */
@Component
public final class ExecutorManager {

    @Bean
    public ExecutorService handlePublishMsg(){
        return new ThreadPoolExecutor(5,20,60000,TimeUnit.SECONDS,new LinkedBlockingQueue<>());
    }

}
