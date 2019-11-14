package com.chenws.iot.mqtt.server;

import com.chenws.iot.mqtt.protocol.Process;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
@Component
public class MqttServer {

    @Value("${transport.mqtt.bind_address}")
    private String host;

    @Value("${transport.mqtt.bind_port}")
    private Integer port;

    @Value("${transport.mqtt.netty.boss_group_thread_count}")
    private Integer bossGroupThreadCount;

    @Value("${transport.mqtt.netty.worker_group_thread_count}")
    private Integer workerGroupThreadCount;

    @Value("${transport.mqtt.netty.so_keep_alive}")
    private boolean keepAlive;

    @Value("${transport.mqtt.netty.max_payload_size}")
    private Integer maxPayloadSize;

    @Value("${transport.mqtt.netty.soSndbuf}")
    private Integer soSndbuf;

    @Value("${transport.mqtt.netty.soRcvbuf}")
    private Integer soRcvbuf;

    @Value("${transport.mqtt.netty.soBacklog}")
    private Integer soBacklog;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel serverChannel;

    @Autowired
    private Process process;

    @PostConstruct
    public void init(){
        new Thread(()-> {
            log.info("MQTT starting.");
            bossGroup = new NioEventLoopGroup(bossGroupThreadCount);
            workerGroup = new NioEventLoopGroup(workerGroupThreadCount);
            ServerBootstrap b = new ServerBootstrap();
            MqttTransportHandler handler = new MqttTransportHandler(process);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new MqttDecoder(maxPayloadSize));
                            pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                            pipeline.addLast(handler);
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
                    //设置发送的缓冲大小
                    .childOption(ChannelOption.SO_SNDBUF, soSndbuf)
                    //设置接收的缓冲大小
                    .option(ChannelOption.SO_RCVBUF, soRcvbuf)
                    .option(ChannelOption.SO_BACKLOG, soBacklog);

            try {
                serverChannel = b.bind(host, port).sync().addListener(handler).channel();
            } catch (InterruptedException e) {
                log.info("Mqtt start failed!");
                e.printStackTrace();
            }
            log.info("Mqtt started!");
        }).start();
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Stopping MQTT!");
        try {
            serverChannel.close().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        log.info("MQTT stopped!");
    }

}
