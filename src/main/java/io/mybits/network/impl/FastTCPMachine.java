package io.mybits.network.impl;

import io.mybits.network.Network;
import io.mybits.network.packet.Packet;
import io.mybits.network.packet.PacketOpcode;
import io.mybits.network.packet.PacketType;
import io.mybits.network.packet.impl.VerifyRequestPacket;
import io.mybits.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.zeromq.*;

import java.util.List;
import java.util.Objects;

public class FastTCPMachine {

    private final int port;

    private ZMQ.Socket socket;

    private final Network network;

    private ZCert cert = new ZCert(), clientCert = new ZCert();

    public FastTCPMachine(int port, Network network){
        this.network = network;
        this.port = port;
        generate();
    }

    private void generate(){
        network.getThreads(Constants.MAIN_NETWORK_THREAD).addToFactory(() -> {
            try (ZContext context = new ZContext()) {
                ZAuth auth = new ZAuth(context, "fastTCP");
                auth.setVerbose(true);
                auth.allow("127.0.0.1");

                socket =  context.createSocket(SocketType.REP);
                socket.bind("tcp://*:" + port);
                while (!Thread.currentThread().isInterrupted()) {
                    while (socket.recv(ZMQ.DONTWAIT) != null) {
                        System.out.println("Received request");
                        ZMsg strings = ZMsg.recvMsg(socket);
                        List<String> list = strings.stream().map(ZFrame::toString).toList();
                        sendPacket(Objects.requireNonNull(PacketOpcode.getOpcode(Integer.parseInt(list.get(0)))), list);
                    }
                }
            }
        });
    }

    public void sendPacket(@NotNull PacketOpcode opcode, List<String> data){
        switch(opcode){
            case VERIFY_REQUEST -> {
                System.out.println("Received verify request");
                VerifyRequestPacket packet = new VerifyRequestPacket(opcode, PacketType.ARRAY_STRING, data, this.port);
                network.getPacketHandler().addToQueue(packet);
            }
        }
    }

}
