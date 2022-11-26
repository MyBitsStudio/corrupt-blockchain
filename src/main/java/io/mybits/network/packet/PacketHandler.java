package io.mybits.network.packet;

import io.mybits.network.Network;
import io.mybits.utils.Constants;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.Serial;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PacketHandler implements Serializable {

    @Serial
    private static final long serialVersionUID = 3616876185530236652L;

    private final Queue<Packet> packets = new LinkedList<>();
    private final CopyOnWriteArrayList<Packet> dispose = new CopyOnWriteArrayList<>();


    public PacketHandler() {
    }

    public void addToQueue(Packet packet) {
        synchronized (this) {
            packets.add(packet);
        }
    }

    public Packet poll() {
        synchronized (this) {
            return packets.poll();
        }
    }

    public void startQueue(){
        Network network = Network.singleton();
        synchronized (this) {
            network.getThreads(Constants.QUEUE).scheduleFixedRate(() -> {
                while(!packets.isEmpty()){
                    final Packet[] packet = {poll(), null};
                    if(packet[0] != null) {
                        System.out.println("Running queue");
                        network.getThreads(Constants.QUEUE).addToFactory(() -> {
                            packet[1] = new PacketSecurity(packet[0]).decode().packet();
                            packet[0] = null;

                            if(packet[1].invalid()){
                                System.out.println("Invalid request!");
                                dispose.add(packet[1]);
                                return;
                            }

                            packet[1] = packet[1].handlePacket();

                            while(packet[1].active.get()){}

                            System.out.println("Response Code : "+packet[1].responseCode());
                            switch(packet[1].responseCode()){
                                case 100 -> {

                                }
                                case 105 -> {
                                    switch(packet[1].type){
                                        case ARRAY_STRING -> {
                                            if(packet[1].channel != null){
                                                sendResponse(packet[1].channel, packet[1]);
                                            }
                                        }
                                        default -> {
                                            if(packet[1].datagram != null){
                                                sendResponse(packet[1].datagram, packet[1].returnOpCode(), packet[1].responseMessage());
                                            }
                                        }
                                    }
                                }
                                case 300 -> {

                                }
                                case 500, default -> {
                                    if (packet[1].type == PacketType.ARRAY_STRING) {
                                        if(packet[1].channel != null){
                                            sendResponse(packet[1].channel, packet[1]);
                                        }
                                    } else if (packet[1].datagram != null) {
                                        sendResponse(packet[1].datagram, PacketOpcode.DENIED_PACKET.getOpcode(), packet[1].responseMessage());
                                    }
                                }
                            }
                        });
                    }

                }
            }, 1, 1);
        }
    }

    private void sendResponse(DatagramPacket datagram, int returnOpCode, String responseMessage) {

    }

    private void sendResponse(ZMQ.Socket socket, @NotNull Packet packet){
        ZMsg strings = new ZMsg();
        strings.addFirst(""+packet.returnOpCode());
        if(packet.returnOpCode()== 1 || packet.returnOpCode() == 2){
            strings.add(packet.security.encrypt(""+4));
        } else {
            strings.add(packet.security.encrypt(""+5));
            strings.add(packet.security.encrypt(packet.security.header()));
        }
        strings.add(packet.security.encrypt(packet.responseMessage()));
        strings.addLast(packet.security.encrypt(packet.security.footer()));
        strings.send(socket);
    }

    private void sendResponse(@NotNull Channel channel, @NotNull Packet packet){
        List<String> info = new ArrayList<>();
        info.add(""+packet.returnOpCode());
        if(packet.returnOpCode()== 1 || packet.returnOpCode() == 2){
            info.add(packet.security.encrypt(""+4));
        } else {
            info.add(packet.security.encrypt(""+5));
            info.add(packet.security.encrypt(packet.security.header()));
        }
        info.add(packet.security.encrypt(packet.responseMessage()));
        info.add(packet.security.encrypt(packet.security.footer()));
        channel.writeAndFlush(info);
    }

}
