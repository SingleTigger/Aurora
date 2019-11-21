package com.chenws.iot.analyze.service;

/**
 * Created by chenws on 2019/11/18.
 */
public interface PublishMsgAnalyzeService {

    /**
     * 解析publsh消息
     * @param message
     */
    void analyze(String message);
}
