package com.chenws.iot.analyze.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by chenws on 2019/11/18.
 */
@Component
@Slf4j
public class PublishMsgHandler {

    @Async("handlePublishMsg")
    public void handlerMsg(byte[] message){

    }

}
