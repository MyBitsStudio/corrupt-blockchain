package io.mybits.network.packet.impl;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.Transaction;
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
import java.util.Optional;

public class TransactionRequestPacket extends Packet {

    private String response;

    private Transaction transaction;

    public TransactionRequestPacket(PacketOpcode opCode, PacketType type, byte[][] data, int port) {
        super(opCode, type, port);
        setByteData(data);
    }

    public TransactionRequestPacket(PacketOpcode opCode, PacketType type, @NotNull List<String> data, int port) {
        super(opCode, type, port);
        this.port = port;
        setData(data.toArray(new String[0]));
    }

    public TransactionRequestPacket(PacketOpcode opCode, PacketType type, @NotNull List<String> data, Channel channel) {
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
    public TransactionRequestPacket handlePacket() {
        HyperLedger ledger = HyperLedger.singleton();
        System.out.println("DATA - "+ Arrays.toString(data)+"\nPort: "+port);
        String[] info = data[2].split(":");
        Optional<Contract> contract = ledger.getContractByName(info[1]);
        if(contract.isPresent()){
            Contract contracts = contract.get();
            Optional<Wallet> wallets = ledger.getWallet(info[2]);
            if(wallets.isPresent()){
                Wallet wallet = wallets.get();

                SystemTransaction transaction = contracts.getGenesis().sendSystemTransaction(wallet.getPublicKey(), Float.parseFloat(info[3]), contracts);
                if(transaction == null) {
                    System.out.println("Transaction is null");
                } else {
                    ledger.addTransactionToChain(transaction);

                    this.transaction = transaction;

                    response = transaction.getProperty("status")+":"+transaction.getTransactionId()+":"+ wallet.getBalance(contracts.getAddress())+":"+info[4];

                }
            } else {
                System.out.println("Wallet not found");
            }
        } else {
            System.out.println("Contract not found");
        }
        active.set(false);
        return this;
    }

    @Override
    public int responseCode() {
        return transaction != null ? 105 : 500;
    }

    @Override
    public String responseMessage() {
        return transaction != null ? response : "";
    }

    @Override
    public int returnOpCode() {
       return this.transaction == null ? PacketOpcode.TRANSACTION_ERROR.getOpcode() : PacketOpcode.TRANSACTION_RESPONSE.getOpcode();
    }
}
