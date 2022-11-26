package io.mybits.tools;

import io.mybits.hyperledger.block.Block;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.TransactionOutput;
import io.mybits.hyperledger.transaction.impl.SystemTransaction;
import io.mybits.hyperledger.wallet.impl.ServerWallet;
import io.mybits.utils.SerializationManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LedgerStarter {

    public static void main(String[] args) {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());

        List<Block> blockchain = new CopyOnWriteArrayList<>();

        Contract[] contract = {
                new Contract("MyNation Coin"),
                new Contract("MyNation Diamond"),

        };

        SystemTransaction[] transactions = new SystemTransaction[contract.length];

        for(int i = 0; i < contract.length; i++){
            ServerWallet wallet = new ServerWallet(contract[i]);
            transactions[i] = new SystemTransaction(wallet.getPublicKey(), contract[i].getGenesis().getPublicKey(), i == 0 ? 6000000000000.0000001d : 2000000000.000001d, null, contract[i]);
            transactions[i].generateSignature(wallet.getPrivateKey());
            transactions[i].setTransactionId(""+i);
            transactions[i].setContract(contract[i]);
            transactions[i].addToOutputs(new TransactionOutput(transactions[i].getRecipient(), transactions[i].getValue(), transactions[i].getTransactionId(), transactions[i].getContract().getAddress()));
        }

        ConcurrentHashMap<String, TransactionOutput> UTXOs = new ConcurrentHashMap<>();
        for(SystemTransaction transaction : transactions){
            UTXOs.put(transaction.getOutputs().get(0).getID(), transaction.getOutputs().get(0));
        }

        Block genesisBlock = new Block(null, 0.000000d);
        for(SystemTransaction transaction : transactions){
            genesisBlock.addTransaction(transaction);
        }
        genesisBlock.mineBlock(0);
        blockchain.add(genesisBlock);
        Block newBlock = new Block(genesisBlock, 0.000000d);
        newBlock.mineBlock(0);
        blockchain.add(newBlock);

        try {
            SerializationManager.serializeBlock(genesisBlock);
            SerializationManager.serializeBlock(newBlock);
            for(Contract contracts : contract){
                SerializationManager.serializeContract(contracts);
            }
            SerializationManager.storeSerializableClass(UTXOs, new File("data/master/chain/UTXO.corrupt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //new SaveProperties().run(contract);
    }
}
