package io.mybits.network.packet.impl;

import io.mybits.network.packet.Packet;
import io.mybits.network.packet.PacketOpcode;
import io.mybits.network.packet.PacketType;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class WalletCreatePacket extends Packet {



    public WalletCreatePacket(PacketOpcode opCode, PacketType type, byte[][] data, int port) {
        super(opCode, type, port);
        setByteData(data);
    }

    public WalletCreatePacket(PacketOpcode opCode, PacketType type, @NotNull List<String> data, int port) {
        super(opCode, type, port);
        this.port = port;
        setData(data.toArray(new String[0]));
    }

    public WalletCreatePacket(PacketOpcode opCode, PacketType type, @NotNull List<String> data, Channel channel) {
        super(opCode, type, channel);
        this.channel = channel;
        setData(data.toArray(new String[0]));
    }

    public int getPort() {
        return port;
    }

    @Override
    public WalletCreatePacket handlePacket() {
        System.out.println("DATA - "+ Arrays.toString(data)+"\nPort: "+port);

        return this;
    }

    @Override
    public int responseCode() {

        return 500;
    }

    @Override
    public String responseMessage() {
        return "";
    }

    @Override
    public int returnOpCode() {
       return 0;
    }
}
