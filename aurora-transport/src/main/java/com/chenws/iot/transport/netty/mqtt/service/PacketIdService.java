package com.chenws.iot.transport.netty.mqtt.service;

/**
 * Created by chenws on 2019/10/21.
 */
public interface PacketIdService {

    Integer getPacketId();

    void addPacketId(Integer packetId);

}
