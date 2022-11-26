package io.mybits.hyperledger.wallet;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.TransactionOutput;
import io.mybits.utils.Utilities;

import java.io.Serial;
import java.io.Serializable;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Wallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1436408522889140935L;
    protected PrivateKey privateKey;
    protected PublicKey publicKey;
    protected byte[] accessKey;
    protected String[] unencrypted;
    protected final String publicAddress;

    protected final ConcurrentHashMap<String, TransactionOutput> transactions = new ConcurrentHashMap<>();
    protected List<Contract> contracts = new CopyOnWriteArrayList<>();

    public Wallet(){
        generateKeyPair();
        this.publicAddress = "0x"+ Utilities.createRandomString(16);
        contracts.add(0, HyperLedger.singleton.getContract("Block Coin"));
        unencrypted = Mnemonic.buildMnemonicPassword();
    }

    public Wallet(Contract contract){
        generateKeyPair();
        this.publicAddress = "0x"+ Utilities.createRandomString(16);
        contracts.add(0, contract);
    }

    public PublicKey getPublicKey(){ return publicKey;}
    public PrivateKey getPrivateKey(){ return privateKey;}
    public String getPublicAddress(){ return publicAddress;}

    public Contract getContract(String address){
        for (Contract contract : contracts) {
            if (contract.getAddress().equals(address)) return contract;
        }
        return null;
    }

    public Contract getContractById(int id){
        return contracts.get(id);
    }

    public void addContract(Contract contract){
        if(!contracts.contains(contract))
            contracts.add(contract);
    }

    public double getBalance(String contract) {
        double total = 0.000000;
        for (ConcurrentMap.Entry<String, TransactionOutput> item : HyperLedger.getUTXOs().entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey) && UTXO.getContract().equals(contract)) {
                transactions.putIfAbsent(UTXO.getID(),UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    public ConcurrentHashMap<String, TransactionOutput> getTransactions(){ return transactions;}

    protected void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addToTransactionsOverride(String id, TransactionOutput put){
        this.transactions.put(id, put);
    }

    public void generateAccessKey(){
        this.accessKey = Utilities.applyECDSASig(privateKey, Arrays.toString(unencrypted));
        this.unencrypted = null;
    }

    public boolean verifyAccessKey(String[] mnemonic){
        return Utilities.verifyECDSASig(publicKey, Arrays.toString(mnemonic), accessKey);
    }
}
