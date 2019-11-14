package com.chenws.iot.mqtt.server;

import com.chenws.iot.mqtt.coder.ByteBuf2WebSocketEncoder;
import com.chenws.iot.mqtt.coder.WebSocket2ByteBufDecoder;
import com.chenws.iot.mqtt.protocol.Process;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;

/**
 * Created by chenws on 2019/8/31.
 */
@Slf4j
@Component
public class MqttServer {

    @Value("${transport.mqtt.bind_address}")
    private String host;

    @Value("${transport.mqtt.tcp.port}")
    private Integer tcpPort;
    @Value("${transport.mqtt.websocket.port}")
    private Integer websocketPort;

    @Value("${transport.mqtt.ssl.tcp.enabled}")
    private Boolean isSslTcp;

    @Value("${transport.mqtt.ssl.tcp.port}")
    private Integer sslTcpPort;

    @Value("${transport.mqtt.ssl.websocket.enabled}")
    private Boolean isSslWebsocket;

    @Value("${transport.mqtt.ssl.websocket.port}")
    private Integer sslWebsocketPort;

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

    @Autowired
    private Process process;

    private MqttTransportHandler handler;

    @PostConstruct
    public void init(){
        bossGroup = new NioEventLoopGroup(bossGroupThreadCount);
        workerGroup = new NioEventLoopGroup(workerGroupThreadCount);
        handler = new MqttTransportHandler(process);
        new Thread(()-> {
            startMqttTcp(false,tcpPort);
            if(isSslTcp)
                startMqttTcp(true,sslTcpPort);
            startWebsocketServer(false,websocketPort);
            if(isSslTcp)
                startWebsocketServer(true,sslWebsocketPort);
        }).start();
    }

    private void startMqttTcp(Boolean isSsl,Integer port){
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if(isSsl) {
                            pipeline.addLast("ssl", NettySslHandler.getSslHandler(
                                    ch,
                                    false,
                                    "PKCS12",
                                    System.getProperty("user.dir") + File.separator + "aurora-mqtt" + File.separator + "Aurora.pfx",
                                    "123456",
                                    "123456"
                            ));
                        }
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
            Channel channel = b.bind(host, port).sync().addListener(handler).channel();
        } catch (InterruptedException e) {
            log.info("Mqtt{}start failed!",isSsl?" ssl ":" ");
            e.printStackTrace();
        }
        log.info("Mqtt{}started on port {}!",isSsl?" ssl ":" ",isSsl?sslTcpPort:port);
    }

    private void startWebsocketServer(Boolean isSsl, Integer port){
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (isSsl) {
                            pipeline.addLast("ssl", NettySslHandler.getSslHandler(
                                    ch,
                                    false,
                                    "PKCS12",
                                    System.getProperty("user.dir") + File.separator + "aurora-mqtt" + File.separator + "Aurora.pfx",
                                    "123456",
                                    "123456"
                            ));
                        }
                        pipeline.addLast("httpCodec",new HttpServerCodec())
                                .addLast("aggregator",new HttpObjectAggregator(65535))
                                .addLast("compressor ", new HttpContentCompressor())
                                .addLast("webSocketHandler",new WebSocketServerProtocolHandler("/mqtt", "mqtt,mqttv3.1,mqttv3.1.1",true))
                                .addLast("byteBuf2WebSocketEncoder",new ByteBuf2WebSocketEncoder())
                                .addLast("webSocket2ByteBufDecoder",new WebSocket2ByteBufDecoder())
                                .addLast("mqttDecoder", new MqttDecoder(maxPayloadSize))
                                .addLast("mqttEncoder", MqttEncoder.INSTANCE)
                                .addLast("nettyMqttHandler", handler);
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
                //设置发送的缓冲大小
                .childOption(ChannelOption.SO_SNDBUF, soSndbuf)
                //设置接收的缓冲大小
                .option(ChannelOption.SO_RCVBUF, soRcvbuf)
                .option(ChannelOption.SO_BACKLOG, soBacklog);
        try {
            Channel channel = b.bind(host, port).sync().addListener(handler).channel();
        }catch (InterruptedException ex){
            log.info("Mqtt Websocket{}start failed!",isSsl?" ssl ":" ");
        }
        log.info("Mqtt Websocket{}started on port {}!",isSsl?" ssl ":" ",isSsl?sslWebsocketPort:port);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Stopping MQTT!");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        log.info("MQTT stopped!");
    }

}
