package io.mybits.hyperledger.transaction;

import io.mybits.protect.ProtectedDouble;
import io.mybits.utils.Utilities;

import java.io.Serial;
import java.io.Serializable;
import java.security.PublicKey;

public class TransactionOutput implements Serializable {

    @Serial
    private static final long serialVersionUID = 6588771312637040300L;
    protected String id;
    protected PublicKey recipient;
    protected ProtectedDouble value;
    protected String parentTransactionId;
    protected String contractAddress;

    public TransactionOutput(PublicKey recipient, double value, String parentTransactionId, String contract) {
        this.recipient = recipient;
        this.value = new ProtectedDouble(value);
        this.parentTransactionId = parentTransactionId;
        this.id = Utilities.applySha256(Utilities.fromKey(recipient)+ value +parentTransactionId);
        this.contractAddress = contract;
    }

    public boolean isMine(PublicKey publicKey) {
        return Utilities.fromKey(recipient).equals(Utilities.fromKey(publicKey));
    }

    public String getID(){ return this.id;}
    public double getValue(){ return this.value.get();}
    public String getContract(){ return this.contractAddress;}
}
