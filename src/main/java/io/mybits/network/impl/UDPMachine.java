package io.mybits.network.impl;

import io.mybits.network.Network;
import io.mybits.utils.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPMachine {

    private final DatagramSocket socket;
    private byte[] receive;
    private final Network network;
    private final int port;

    public static UDPMachine singleton;

    public UDPMachine(Network network, int port)  {
        this.network = network;
        try {
            this.socket = new DatagramSocket(this.port = port);
            this.socket.setReuseAddress(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        this.receive = new byte[65535];
        singleton = this;
        singleton.start();
    }

    public void start(){
        network.getThreads(Constants.MAIN_NETWORK_THREAD).addToFactory(() -> {
            try {
                while (true) {
                    synchronized (this) {
                        receive = new byte[65535];

                        DatagramPacket incomingPacket = new DatagramPacket(receive, receive.length);
                        socket.receive(incomingPacket);


                        //System.out.println("UDP: " + byteToString(incomingPacket.getData()));

                        receive = new byte[65535];
                        incomingPacket = null;
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception occured");
            }
        });
    }


}
