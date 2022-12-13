package io.mybits.network.handler;

import io.mybits.network.Network;
import io.mybits.network.packet.Packet;
import io.mybits.network.packet.PacketOpcode;
import io.mybits.network.packet.PacketType;
import io.mybits.network.packet.impl.TransactionRequestPacket;
import io.mybits.network.packet.impl.VerifyRequestPacket;
import io.mybits.network.packet.impl.WalletCreatePacket;
import io.mybits.utils.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;

public class TCPObjectHandler extends SimpleChannelInboundHandler<Object> {

    public final Network network;

    public TCPObjectHandler(Network network) {
        super();
        this.network = network;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) {

        network.getThreads(Constants.TCP_THREAD).addToFactory(() -> {
            if(o instanceof ArrayList){

                try {
                    ArrayList<String> info = (ArrayList<String>) o;
                    System.out.println("Received object : "+o);
                    PacketOpcode opcode = PacketOpcode.getOpcode(Integer.parseInt(info.get(0)));
                    if(opcode != null){
                        switch(opcode.getOpcode()){
                            case 1 -> {
                                VerifyRequestPacket packet = new VerifyRequestPacket(opcode, PacketType.ARRAY_STRING, info, ctx.channel());
                                network.getPacketHandler().addToQueue(packet);
                            }
                            case 7 -> {
                                WalletCreatePacket packet = new WalletCreatePacket(opcode, PacketType.ARRAY_STRING, info, ctx.channel());
                                network.getPacketHandler().addToQueue(packet);
                            }
                            case 12 -> {
                                TransactionRequestPacket packet = new TransactionRequestPacket(opcode, PacketType.ARRAY_STRING, info, ctx.channel());
                                network.getPacketHandler().addToQueue(packet);
                            }
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }

            } else {

            }
        });
    }
}
