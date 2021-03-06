package com.chenws.iot.mqtt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by chenws on 2019/8/31.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.chenws")
public class AuroraMqttApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuroraMqttApplication.class, args);
    }

}
