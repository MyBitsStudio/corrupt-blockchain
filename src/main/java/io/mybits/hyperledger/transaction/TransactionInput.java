package io.mybits.hyperledger.transaction;

import java.io.Serial;
import java.io.Serializable;

public class TransactionInput implements Serializable {

    @Serial
    private static final long serialVersionUID = 5951355783309239533L;
    public String transactionOutputId;
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
