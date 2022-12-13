package io.mybits.hyperledger;

import io.mybits.hyperledger.block.Block;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.Transaction;
import io.mybits.hyperledger.transaction.TransactionOutput;
import io.mybits.hyperledger.wallet.Mnemonic;
import io.mybits.hyperledger.wallet.Wallet;
import io.mybits.hyperledger.wallet.impl.PlayerWallet;
import io.mybits.hyperledger.wallet.impl.ServerWallet;
import io.mybits.services.BlockGenerator;
import io.mybits.threads.BlockchainThreads;
import io.mybits.utils.LedgerLogs;
import io.mybits.utils.OperationLocks;
import io.mybits.utils.SerializationManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.security.Security;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class HyperLedger implements Serializable {
    @Serial
    private static final long serialVersionUID = 3980343513468618571L;

    private final List<Block> blockchain = new CopyOnWriteArrayList<>();
    private static Map<String, TransactionOutput> UTXOs = new ConcurrentHashMap<>();
    private final BlockchainThreads threads = new BlockchainThreads();

    private final List<Contract> contracts = new CopyOnWriteArrayList<>();
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    private final List<Wallet> wallets = new CopyOnWriteArrayList<>();

    private final OperationLocks locks = new OperationLocks();
    private final AtomicBoolean booting = new AtomicBoolean(true);
    public static HyperLedger singleton;


    public static HyperLedger singleton(){
         if(singleton == null){
             singleton = new HyperLedger();
         }
         return singleton;
    }

    public List<Block> getChain() {
        synchronized (blockchain) {
            return blockchain;
        }}
    public static Map<String, TransactionOutput> getUTXOs(){
            return UTXOs;}
    public boolean isBooting(){ return this.booting.get();}
    public Block getLatestBlock(){
        synchronized (blockchain) {
            return blockchain.get(blockchain.size() - 1);
        }}
    public List<Contract> getContracts() {
        synchronized (contracts) {
            return contracts;
        }}
    public String getProperty(String key) {return properties.get(key); }
    public BlockchainThreads getThreads(){ return this.threads;}
    public Contract getContract(String address){
        synchronized (contracts) {
            for (Contract contract : contracts) {
                if (contract.getAddress().equals(address)) {
                    return contract;
                }
            }
        }
        return null;
    }

    public Contract getContractByCoin(String coin){
        synchronized (contracts) {
            for (Contract contract : contracts) {
                if (contract.getTokenName().equals(coin)) {
                    return contract;
                }
            }
        }
        return null;
    }


    public void start(){
        LedgerLogs.logBooting("Starting Hyperledger", LedgerLogs.INFO, true);
        startVariables();
        setProperties();
        backgroundTasks();
        generate();
    }

    private void backgroundTasks(){
        locks.setLock(12);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for(Contract contract : contracts){
                    SerializationManager.serializeContract(contract);
                }
            }).thenRunAsync(() -> {
                for(Block block : blockchain){
                    SerializationManager.serializeBlock(block);
                }
            }).thenRunAsync(() -> locks.unlock(12));
            future.join();
            while(locks.isLocked(12)){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        LedgerLogs.logBooting("Background Tasks Set!", LedgerLogs.INFO, true);
    }

    public Optional<Contract> getContractByName(String name){
        synchronized (contracts) {
            for (Contract contract : contracts) {
                if (contract.getTokenName().equals(name)) {
                    return Optional.of(contract);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Wallet> getWallet(String address){
        synchronized (wallets) {
            for (Wallet wallet : wallets) {
                if (wallet.getPublicAddress().equals(address)) {
                    return Optional.of(wallet);
                }
            }
        }
        return Optional.empty();
    }

    protected void startVariables() {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
        Mnemonic.initPhrases();
        LedgerLogs.logBooting("Variables Set!", LedgerLogs.INFO, true);
    }

    private void setProperties(){
        this.properties.put("name", "MyBits");
        this.properties.put("symbol", "MBT");
        this.properties.put("version", "0.0.1");
        this.properties.put("description", "MyBits HyperLedger is a blockchain based platform for the management of games and business activities.");
        this.properties.put("author", "Corrupt - MyBitsStudio Founder");
        this.properties.put("email", "mybitsstudiogaming@gmail.com");
        LedgerLogs.logBooting("Properties Set!", LedgerLogs.INFO, true);
    }

    private void generate(){
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            locks.setLock(0);
            threads.addToFactory(this::startBlocks);
            locks.unlock(0);
        }).thenRunAsync(() -> {
            locks.setLock(1);
            threads.addToFactory(this::startContracts);
            locks.unlock(1);
        }).thenRunAsync(() -> {
            locks.setLock(2);
            threads.addToFactory(this::startWallets);
            locks.unlock(2);
        }).thenRunAsync(() -> {
            locks.setLock(3);
            threads.addToFactory(this::loadUTXOS);
            locks.unlock(3);
        }).thenRunAsync(() -> {
            locks.setLock(4);
            threads.addToFactory(() -> new BlockGenerator(this));
            locks.unlock(4);
        });
        future.join();
        LedgerLogs.logBooting("Generated!", LedgerLogs.INFO, true);
        while(locks.isLocked(0) || locks.isLocked(1) || locks.isLocked(2) || locks.isLocked(3) || locks.isLocked(4) || locks.isLocked(5)){
        }
        this.booting.set(false);
    }

    private void startBlocks(){
        File file = new File("data/master/chain/blocks/");
        if(!file.isDirectory() || Objects.requireNonNull(file.listFiles()).length == 0){
            failSafe("Blocks");
            return;
        }
        for(File files : Objects.requireNonNull(file.listFiles())){
            Block block = SerializationManager.loadBlock(files);
            blockchain.add(block);
        }
        Collections.sort(blockchain);
        LedgerLogs.logBooting("Blocks have loaded!", LedgerLogs.INFO, true);
        for(Block block : blockchain){
            LedgerLogs.logBooting(""+block, LedgerLogs.INFO, true);
            for(Transaction transaction : block.getTransactions()){
                LedgerLogs.logBooting(""+transaction, LedgerLogs.INFO, true);
            }
        }
    }

    private void startContracts(){
        File contracts = new File("data/master/chain/contract");
        if(!contracts.isDirectory() || Objects.requireNonNull(contracts.listFiles()).length == 0){
            failSafe("Contracts");
            return;
        }
        for(File file : Objects.requireNonNull(contracts.listFiles())){
            Contract contract = SerializationManager.loadContract(file);
            this.contracts.add(contract);
        }
        LedgerLogs.logBooting("Contracts have loaded!", LedgerLogs.INFO, true);
    }

    private void startWallets(){
        File wallets = new File("data/master/chain/wallet/");
        if(!wallets.isDirectory() || Objects.requireNonNull(wallets.listFiles()).length == 0) {
            failSafe("Wallets");
            return;
        }

        for(File file : Objects.requireNonNull(wallets.listFiles())){
            Wallet wallet = SerializationManager.loadWallet(file);
            if(wallet instanceof PlayerWallet playerWallet){
                this.wallets.add(playerWallet);
            } else if(wallet instanceof ServerWallet serverWallet){
                this.wallets.add(serverWallet);
            }
        }
    }

    private void loadUTXOS(){
        File uto = new File("data/master/chain/UTXO.corrupt");
        if(uto.exists()){
            try {
                UTXOs = (ConcurrentHashMap<String, TransactionOutput>) SerializationManager.loadSerializedFile(new File("data/master/chain/UTXO.corrupt"));
                LedgerLogs.logBooting("UTXOs have loaded!", LedgerLogs.INFO, true);
            } catch (IOException | ClassNotFoundException e) {
                LedgerLogs.logBooting("UTXOs Errors!", LedgerLogs.ERROR, true);
                e.printStackTrace();
            }
        } else {
            failSafe("UTXOS");

            //System.exit(0);
        }
    }

    private void failSafe(String info){
        LedgerLogs.logBooting("FAIL SAFE HIT!\n"+info, LedgerLogs.ERROR, true);
    }

    public void addTransactionToChain(Transaction transaction){
        Block block = getLatestBlock();
        CompletableFuture<Void> future =
             CompletableFuture.runAsync(() ->
             block.addTransaction(transaction))
                     .thenRunAsync(() -> block.mineBlock(0))
                     .thenRunAsync(() -> {
                        SerializationManager.serializeBlock(block);
                        try{
                            SerializationManager.storeSerializableClass((Serializable) UTXOs, new File("data/master/chain/UTXO.corrupt"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }})
                     .thenRunAsync(transaction::onComplete);
        future.join();
    }
}
