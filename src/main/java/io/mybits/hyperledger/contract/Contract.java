package io.mybits.hyperledger.contract;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.transaction.TransactionOutput;
import io.mybits.hyperledger.wallet.impl.ContractWallet;
import io.mybits.hyperledger.wallet.impl.GasWallet;
import io.mybits.hyperledger.wallet.impl.MintWallet;
import io.mybits.utils.Utilities;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Contract implements Serializable {

    @Serial
    private static final long serialVersionUID = 6646731582432095048L;
    private String address, hash;
    private final String tokenName;

    private final ContractWallet genesis;
    private final GasWallet gasProvider;
    private final MintWallet minting;
    private final Map<String, TransactionOutput> transactions = new ConcurrentHashMap<>();

    public Contract(String tokenName){
        this.tokenName = tokenName;
        this.address = "6xf"+ Utilities.createRandomString(25);
        this.genesis = new ContractWallet(this);
        this.gasProvider = new GasWallet(this);
        this.minting = new MintWallet(this);
        generateHash();
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public ContractWallet getGenesis() {return genesis;}
    public GasWallet getGasProvider() {return gasProvider;}
    public MintWallet getMinting() {return minting;}

    public String getTokenName() {
        return tokenName;
    }

    public void generateHash(){
        this.hash = Utilities.applySha256(
                address + genesis.getPublicAddress() +
                        Utilities.createRandomString(12) + tokenName
        );
    }

    public double getBalance() {
        double total = 0.000001;
        for (ConcurrentMap.Entry<String, TransactionOutput> item : HyperLedger.getUTXOs().entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.getContract().equals(address)) {
                transactions.putIfAbsent(UTXO.getID(),UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }





}
