package io.mybits.hyperledger.transaction.impl;

import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.Transaction;
import io.mybits.hyperledger.transaction.TransactionIdentity;
import io.mybits.hyperledger.transaction.TransactionInput;
import io.mybits.protect.ProtectedDouble;
import io.mybits.utils.Utilities;

import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class SystemTransaction extends Transaction {

    @Serial
    private static final long serialVersionUID = -265008157138148006L;

    public SystemTransaction(PublicKey from, PublicKey to, double value, ArrayList<TransactionInput> inputs, Contract contract){
        super();
        this.sender = from;
        this.recipient = to;
        this.value = new ProtectedDouble(value);
        this.inputs = inputs;
        outputs = new ArrayList<>();
        this.contract = contract;
    }

    @Override
    public void generateSignature(PrivateKey key) {
        String data = Utilities.fromKey(sender) + Utilities.fromKey(recipient) + value + TransactionIdentity.SYSTEM + 0x0;
        signature = Utilities.applyECDSASig(key,data);
    }

    @Override
    public boolean verifySignature() {
        String data = Utilities.fromKey(sender) + Utilities.fromKey(recipient) + value + TransactionIdentity.SYSTEM + 0x0;
        return Utilities.verifyECDSASig(sender, data, signature);
    }

    @Override
    public void onComplete() {

    }

}
