package io.mybits.network.packet;

import io.mybits.protect.ProtectedBoolean;
import io.mybits.protect.ProtectedDouble;
import io.netty.channel.Channel;
import org.zeromq.ZMQ;

import java.net.DatagramPacket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Packet {

    protected PacketOpcode opCode;
    protected PacketType type;
    protected byte[][] byteData;
    protected String[] data;
    protected List<String> listData;

    protected ProtectedBoolean active = new ProtectedBoolean(true);
    protected List<PacketProperties> properties = new CopyOnWriteArrayList<>();

    protected Channel channel;
    protected DatagramPacket datagram;

    protected PacketSecurity security;

    protected int port;

    public Packet(PacketOpcode opCode, PacketType type) {
        this.opCode = opCode;
        this.type = type;
    }

    public Packet(PacketOpcode opCode, PacketType type, int port) {
        this.opCode = opCode;
        this.type = type;
        this.port = port;
    }

    public Packet(PacketOpcode opCode, PacketType type, Channel channel) {
        this.opCode = opCode;
        this.type = type;
        this.channel = channel;
    }

    public Packet(){

    }

    public abstract Packet handlePacket();
    public abstract int responseCode();

    public abstract String responseMessage();

    public abstract int returnOpCode();

    public void setByteData(byte[][] data) {
        this.byteData = data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public PacketOpcode getOpCode() {
        return opCode;
    }

    public PacketType getType() {
        return type;
    }

    public String[] getData() {
        return data;
    }

    public byte[][] getByteData() {
        return byteData;
    }

    public boolean invalid(){ return properties.size() > 0; }

    public void setPort(int port) {
        this.port = port;
    }
}
