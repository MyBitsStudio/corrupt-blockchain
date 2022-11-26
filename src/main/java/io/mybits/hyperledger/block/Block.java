package io.mybits.hyperledger.block;

import io.mybits.hyperledger.transaction.Transaction;
import io.mybits.protect.ProtectedDouble;
import io.mybits.protect.ProtectedInteger;
import io.mybits.protect.ProtectedLong;
import io.mybits.utils.Utilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Block implements Serializable, Comparable<Block>{

    @Serial
    private static final long serialVersionUID = -7447778458814591611L;
    private String hash, stage;
    private final String previousHash;
    private String merkleRoot;
    private List<Transaction> transactions;
    private final List<Transaction> previousTransactions;
    private ProtectedLong timeStamp;
    private ProtectedInteger nonce;
    private final ProtectedInteger chainLink;
    private final ProtectedDouble balance;

    public Block(Block previous, double balance){
        if(previous == null) {
            this.previousHash = "0";
            this.previousTransactions = null;
            this.chainLink = new ProtectedInteger(0);
        }else{
            this.previousHash = previous.getHash();
            this.previousTransactions = previous.getTransactions();
            this.chainLink = new ProtectedInteger(previous.chainLink.get() + 1);
        }
        this.balance = new ProtectedDouble(balance);
        this.stage = "OPEN";
        startBlock();
        calculateBlock();
    }

    public String getHash(){ return this.hash;}
    public String getPreviousHash(){ return this.previousHash;}
    public String getMerkleRoot(){ return this.merkleRoot;}
    public List<Transaction> getTransactions(){ return this.transactions;}
    public List<Transaction> getPreviousTransactions(){ return this.previousTransactions;}
    public long getTimeStamp(){ return this.timeStamp.get();}
    public int getNonce(){ return this.nonce.get();}
    public int getChainLink(){ return this.chainLink.get();}
    public double getBalance(){ return this.balance.get();}

    public void setStage(String stage){ this.stage = stage;}

    public String calculateHash() {
        return Utilities.applySha256(
                previousHash +
                        timeStamp +
                        nonce +
                        merkleRoot
        );
    }

    @Override
    public String toString(){
        return "-----*----- Block "+chainLink.get()+" -----*-----\n" +
                "Transactions : "+transactions.size()+"\n" +
                "Hash : "+hash+"\n" +
                "Time : "+timeStamp.get()+"\n" +
                "Previous Block : "+previousHash+"\n" +
                "Balance : "+balance.get()+"\n";
    }

    protected void startBlock(){
        transactions = new CopyOnWriteArrayList<>();
        timeStamp = new ProtectedLong(new Date().getTime());
    }

    private void calculateBlock(){
        nonce = new ProtectedInteger(0);
        hash = calculateHash();
    }

    public void addTransaction(Transaction transaction) {
        if(transaction == null) {
            //LogHelper.logChain("Transaction Null ", LogHelper.ERROR, true);
            return;
        }
        if((!"0".equals(previousHash))) {
            if((!transaction.processTransaction())) {
               // LogHelper.logChain("Transaction "+transaction.getTransactionId() +" wasn't processed", LogHelper.ERROR, true);
                return;
            }
        }

        transaction.setBlock(this.hash);
        transactions.add(transaction);
        //LogHelper.logChain("Transaction Successfully added to Block" + hash, LogHelper.SUCCESS, true);
    }

    public void mineBlock(int difficulty) {
        merkleRoot = Utilities.getMerkleRoot(transactions);
        String target = Utilities.getDificultyString(difficulty);
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce.increment();
            hash = calculateHash();
        }
       // LogHelper.logChain("Block Mined : "+this.getHash(), LogHelper.DEBUG, true);
    }

    @Override
    public int compareTo(@NotNull Block o) {
        return this.chainLink.subtract(o.chainLink.get());
    }

    public boolean checkTransaction(Transaction trans){
        return this.transactions.contains(trans);
    }

    public Block blockDigest(){
        this.stage = "MINING";
        int amount = this.transactions.size();
        double first = balance.get() / amount;

        return this;
    }
}
