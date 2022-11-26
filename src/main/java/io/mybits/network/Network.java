package io.mybits.network;

import io.mybits.network.impl.FastTCPMachine;
import io.mybits.network.impl.TCPMachine;
import io.mybits.network.impl.UDPMachine;
import io.mybits.network.packet.PacketHandler;
import io.mybits.threads.NetworkThreads;
import io.mybits.utils.Constants;
import io.mybits.utils.LedgerLogs;
import io.mybits.utils.Utilities;
import io.netty.channel.Channel;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Network {

    private final UDPMachine[] udpMachines = new UDPMachine[Constants.clientPorts.length / 2];
    private final TCPMachine[] tcpMachines = new TCPMachine[Constants.fastPorts.length];
    private final NetworkThreads[] threads = new NetworkThreads[6];

    private final PacketHandler packetHandler = new PacketHandler();

    private final AtomicInteger count = new AtomicInteger(Utilities.random(13, 765));

    private final Map<Channel, List<String>> serials = new ConcurrentHashMap<>();
    private static Network network;

    private final AtomicBoolean booting = new AtomicBoolean(true);

    public static Network singleton(){
        if(network == null)
            network = new Network();
        return network;
    }

    public void start(){
        NetworkConstants.initKeys();
        generate();
    }

    public void generate(){
        LedgerLogs.logBooting("Generating network", LedgerLogs.INFO, true);
        int[] clientPorts = Constants.clientPorts;
        for(int i = 0; i < 6; i++){
            threads[i] = new NetworkThreads();
        }
        threads[0].addToFactory(() -> {
            for(int i = 0; i < clientPorts.length; i+=2){
                try {
                    udpMachines[i / 2] = new UDPMachine(this, clientPorts[i]);
                    LedgerLogs.logBooting("Client machine started! [port="+clientPorts[i]+"] - [machine="+i/2+"]", LedgerLogs.INFO, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        int[] fastPorts = Constants.fastPorts;

            for(int i = 0; i < fastPorts.length; i++){
                try {
                    int finalI = i;
                    threads[0].addToFactory(() -> tcpMachines[finalI] = new TCPMachine(fastPorts[finalI], this));
                    LedgerLogs.logBooting("Fast machine started! [port="+fastPorts[i]+"] - [machine="+i+"]", LedgerLogs.INFO, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        booting.set(false);
    }

    public void startQueue(){
        packetHandler.startQueue();
    }

    public UDPMachine getUDPMachine(int machine){
        return udpMachines[machine];
    }

    public TCPMachine getTCPMachine(int machine){
        return tcpMachines[machine];
    }

    public NetworkThreads getThreads(int type){
        return threads[type];
    }

    public PacketHandler getPacketHandler(){return packetHandler;}

    public AtomicInteger getCount(){return count;}

    public String radix(){return new BigInteger(""+count).toString(6);}
    public void increment(){
        count.incrementAndGet();
    }

    public boolean isBooting(){return booting.get();}

    public void runLater(){
        packetHandler.startQueue();
        threads[0].scheduleFixedRate(System::gc, 900, 900);

    }

    public Map<Channel, List<String>> getSerials(){return serials;}

    public List<String> getSerials(Channel channel){return serials.get(channel);}

    public void addSerials(Channel channel, List<String> serials){this.serials.put(channel, serials);}

}
