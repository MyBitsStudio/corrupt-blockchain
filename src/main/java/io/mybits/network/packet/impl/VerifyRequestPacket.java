package io.mybits.network.packet.impl;

import io.mybits.network.Network;
import io.mybits.network.packet.Packet;
import io.mybits.network.packet.PacketOpcode;
import io.mybits.network.packet.PacketType;
import io.mybits.protect.Serials;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerifyRequestPacket extends Packet {

    private String serial = "", superSerial = "";

    public VerifyRequestPacket(PacketOpcode opCode, PacketType type, byte[][] data, int port) {
        super(opCode, type, port);
        setByteData(data);
    }

    public VerifyRequestPacket(PacketOpcode opCode, PacketType type, @NotNull List<String> data, int port) {
        super(opCode, type, port);
        this.port = port;
        setData(data.toArray(new String[0]));
    }

    public VerifyRequestPacket(PacketOpcode opCode, PacketType type, @NotNull List<String> data, Channel channel) {
        super(opCode, type, channel);
        this.channel = channel;
        setData(data.toArray(new String[0]));
    }

    public int getPort() {
        return port;
    }

    @Override
    public VerifyRequestPacket handlePacket() {
        System.out.println("DATA - "+ Arrays.toString(data)+"\nPort: "+port);
        String[] split = data[1].split(":");
        serial = split[0];
        superSerial = Serials.superSerial(serial);
        System.out.println("Serial: " + serial + " SuperSerial: " + superSerial);
        Network network = Network.singleton();
        network.addSerials(this.channel, new ArrayList<>());
        network.getSerials(this.channel).add(serial);
        network.getSerials(this.channel).add(superSerial);
        System.out.println("Serials: " + network.getSerials(this.channel));
        active.set(false);
        return this;
    }

    @Override
    public int responseCode() {
        if(!superSerial.equals("") && !serial.equals("")){
            return 105;
        }
        return 500;
    }

    @Override
    public String responseMessage() {
        return serial+":"+ Network.singleton().radix()+":"+superSerial;
    }

    @Override
    public int returnOpCode() {
        if(responseCode() == 105) {
            if (type == PacketType.ARRAY_STRING) {
                return PacketOpcode.REQUEST_TCP_VERIFIED.getOpcode();
            } else {
                return PacketOpcode.REQUEST_UDP_VERIFIED.getOpcode();
            }
        } else if (type == PacketType.ARRAY_STRING) {
            return PacketOpcode.REQUEST_TCP_DENIED.getOpcode();
        } else {
            return PacketOpcode.REQUEST_UDP_DENIED.getOpcode();
        }
    }
}
