package com.chenws.iot.analyze.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by chenws on 2019/11/18.
 */
@Component
@Slf4j
public class PublishMsgHandler {

    @Autowired
    private RedisTemplate redisTemplate;

    @Async("handlePublishMsg")
    public void handlerMsg(byte[] message){
        //都当成十六进制处理
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
            int head = dataInputStream.readInt();
//            head
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
