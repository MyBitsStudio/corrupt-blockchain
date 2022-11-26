package io.mybits.hyperledger.wallet.impl;

import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.transaction.TransactionInput;
import io.mybits.hyperledger.transaction.TransactionOutput;
import io.mybits.hyperledger.transaction.impl.SystemTransaction;
import io.mybits.hyperledger.wallet.Wallet;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;

public class GasWallet extends Wallet {

    @Serial
    private static final long serialVersionUID = 3563775633091338291L;

    public GasWallet(Contract contract){super(contract);}

    public SystemTransaction sendSystemTransaction(PublicKey _recipient, float value, @NotNull Contract contract){
        if(getBalance(contract.getAddress()) < value) {
           // LogHelper.logChain("Funds not available "+this.publicAddress+":"+value, LogHelper.ERROR, true);
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: transactions.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getID()));
            if(total > value) break;
        }

        SystemTransaction newTransaction = new SystemTransaction(publicKey, _recipient , value, inputs, contract);
        newTransaction.setContract(contract);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            transactions.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
}
