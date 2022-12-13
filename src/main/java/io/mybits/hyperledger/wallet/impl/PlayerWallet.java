package io.mybits.hyperledger.wallet.impl;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.item.Item;
import io.mybits.hyperledger.transaction.TransactionInput;
import io.mybits.hyperledger.transaction.TransactionOutput;
import io.mybits.hyperledger.transaction.impl.SystemTransaction;
import io.mybits.hyperledger.wallet.Wallet;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerWallet extends Wallet {

    @Serial
    private static final long serialVersionUID = -2526399703241862999L;

    private final List<Item> validated = new CopyOnWriteArrayList<>(), waiting = new CopyOnWriteArrayList<>();

    public PlayerWallet(Contract contract){
        super(contract);
        addContracts();
    }

    private void addContracts(){
        for(Contract contract : HyperLedger.singleton().getContracts()){
            if(!contracts.contains(contract))
                contracts.add(contract);
        }
    }

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
