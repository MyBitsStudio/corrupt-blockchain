package io.mybits.network.packet.impl;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.impl.SystemTransaction;
import io.mybits.hyperledger.wallet.Wallet;
import io.mybits.hyperledger.wallet.impl.PlayerWallet;
import io.mybits.hyperledger.wallet.impl.ServerWallet;
import io.mybits.network.packet.Packet;
import io.mybits.network.packet.PacketOpcode;
import io.mybits.network.packet.PacketType;
import io.mybits.utils.SerializationManager;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class WalletCreatePacket extends Packet {

    private String response;
    private Wallet wallet;

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
        this.listData = data;
    }

    public int getPort() {
        return port;
    }

    //Request REQUEST:TYPE:AMOUNT
    @Override
    public WalletCreatePacket handlePacket() {
        HyperLedger ledger = HyperLedger.singleton();
        System.out.println("DATA - "+ Arrays.toString(data)+"\nPort: "+port);
        String[] info = data[2].split(":");
        switch(Integer.parseInt(info[1])){
            case 0 -> wallet = new ServerWallet(ledger.getContractByCoin("Block Coin"));
            case 1 -> wallet = new PlayerWallet(ledger.getContractByCoin("Block Coin"));
        }
        if(wallet != null){
            System.out.println("Wallet Created: "+wallet.getPublicAddress());
            if(!info[2].equals("-1")){
                Contract contract = ledger.getContractByCoin("Block Coin");
                SystemTransaction trans = contract.getGenesis().sendSystemTransaction(wallet.getPublicKey(), Float.parseFloat(info[2]), contract);
                ledger.addTransactionToChain(trans);
                SerializationManager.serializeWallet(wallet);
                System.out.println("Transaction : "+trans);
            }
            response = wallet.getPublicAddress()+":"+wallet.getBalance(ledger.getContractByCoin("Block Coin").getAddress())+":"+wallet.getTransactions().size()+":"+info[3];
        }
        active.set(false);
        return this;
    }

    @Override
    public int responseCode() {
        return wallet != null ? 105 : 500;
    }

    @Override
    public String responseMessage() {
        return wallet != null ? response : "";
    }

    @Override
    public int returnOpCode() {
       return this.wallet == null ? PacketOpcode.WALLET_ERROR.getOpcode() : PacketOpcode.WALLET_RESPONSE.getOpcode();
    }
}
