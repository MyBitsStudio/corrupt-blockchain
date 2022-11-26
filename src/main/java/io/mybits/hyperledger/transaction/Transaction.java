package io.mybits.hyperledger.transaction;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.protect.ProtectedDouble;
import io.mybits.protect.ProtectedInteger;
import io.mybits.utils.Utilities;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = -7213987657708470764L;
    protected String transactionId;
    protected PublicKey sender, recipient;
    protected ProtectedDouble gasFee = new ProtectedDouble(0.000000);
    protected byte[] signature;
    protected static ProtectedInteger sequence = new ProtectedInteger(0);
    protected String block, blockNode;

    protected Contract contract;

    protected ArrayList<TransactionInput> inputs;
    protected ArrayList<TransactionOutput> outputs;

    //Tokens Sending
    protected ProtectedDouble value;

    protected final Map<String, String> properties = new ConcurrentHashMap<>();

    public double getValue(){ return this.value.get();}
    public String getTransactionId(){ return this.transactionId;}
    public PublicKey getSender(){ return this.sender;}
    public PublicKey getRecipient() { return this.recipient;}
    public ArrayList<TransactionOutput> getOutputs(){ return this.outputs;}
    public void setBlock(String block){ this.block = block;}
    public String getBlock(){ return this.block;}
    public void setBlockNode(String blockNode){ this.blockNode = blockNode;}
    public String getBlockNode(){ return this.blockNode;}

    public Contract getContract(){ return this.contract;}
    public void setContract(Contract contract){ this.contract = contract;}

    public Transaction(){}

    /**
     * Functions
     *
     */

    @Nullable String getHash() {
        sequence.increment();
        return Utilities.applySha256(
                Utilities.fromKey(sender) +
                        Utilities.fromKey(recipient) +
                        value + sequence
        );
    }

    public boolean processTransaction() {

       // LogHelper.logChain("Processing Transaction ", LogHelper.INFO, true);

        if(!verifySignature()) {
            //LogHelper.logChain("#Transaction Signature failed to verify", LogHelper.ERROR, true);
            return false;
        }

        //Gathers transaction inputs (Making sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = HyperLedger.getUTXOs().get(i.transactionOutputId);

        }

        //LogHelper.logChain("UTXOs Inputs 1 : "+MasterChain.getUTXOs().toString(), LogHelper.DEBUG, true);

        //Checks if transaction is valid:
        if(getInputsValue() < 0.000005) {
            //LogHelper.logChain("Transaction Inputs too small: " + getInputsValue(), LogHelper.ERROR, true);
            return false;
        }

        //Generate transaction outputs:
        double leftOver = getInputsValue() - (value.subtract(gasFee.get())); //get value of inputs then the left over change:
        transactionId = getHash();
        outputs.add(new TransactionOutput( this.recipient, (value.subtract(gasFee.get())),transactionId, contract.getAddress())); //send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver ,transactionId, contract.getAddress())); //send the left over 'change' back to sender
        outputs.add(new TransactionOutput( this.contract.getGasProvider().getPublicKey(), gasFee.get(),transactionId, contract.getAddress())); //send gas fee to contract
       // LogHelper.logChain("Transaction ID : "+transactionId, LogHelper.DEBUG, true);

       // LogHelper.logChain("Outputs : "+outputs, LogHelper.DEBUG, true);
        //Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            HyperLedger.getUTXOs().put(o.id , o);
        }

        //LogHelper.logChain("UTXO 2  : "+MasterChain.getUTXOs(), LogHelper.DEBUG, true);

        //Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            //MasterChain.getUTXOs().remove(i.UTXO.id);
        }
        //LogHelper.logChain("UTXO 3  : "+MasterChain.getUTXOs(), LogHelper.DEBUG, true);

        return true;
    }

    protected double getInputsValue() {
        double total = 0.000000;
        for(TransactionInput i : inputs) {
            total += i.UTXO.value.get();
        }
        return total;
    }

    /**
     * Generates Transaction Signature.
     * Basic Operation :
     *
     *  String data = StringHelper.getStringFromKey(sender) + StringHelper.getStringFromKey(recipient) + value;
     *  signature = Crypto.applyECDSASig(key,data);
     *
     *  Be careful when changing signatures as they must validate.
     * @param key -- Private key of Sender for Signing Transaction
     */

    protected abstract void generateSignature(PrivateKey key);


    /**
     *  User to verify the signature before proceeding. Make sure its set right to generating or transaction will fail
     *
     *  Basic Operation :
     *
     *  String data = StringHelper.getStringFromKey(sender) + StringHelper.getStringFromKey(recipient) + value;
     *  return Crypto.verifyECDSASig(sender, data, signature);
     *
     *  Be careful this checks right with validation.
     *
     */

    public abstract boolean verifySignature();

    /**
     * This is ran after the transaction has been verified on the chain
     * This can be used to send items after transaction is verified
     */

    public abstract void onComplete();


    protected double getOutputsValue() {
        double total = 0.000000;
        for(TransactionOutput o : outputs) {
            total += o.value.get();
        }
        return total;
    }

    public void setTransactionId(String id){
        this.transactionId = id;
    }

    public void addToOutputs(TransactionOutput output){
        this.outputs.add(output);
    }

    public void addProperty(String key, String value){
        this.properties.put(key, value);
    }

    public String getProperty(String key){
        return this.properties.get(key);
    }

    @Override
    public String toString(){
        return "Transaction ID : " + this.transactionId + "\n " +
                "Sender : " + Utilities.fromKey(sender) + " \n" +
                "Recipient : " + Utilities.fromKey(recipient) + " \n" +
                "Value : " + value.get() + " \n" +
                "Gas Fee : " + gasFee.get() + " \n" +
                "Contract : " + contract.getAddress() + " \n" +
                "Contract Hash : " + contract.getHash() + " \n" +
                "Properties : " + properties + " \n" +
                "Block : " + block + " \n" +
                "Sequence : " + sequence.get() + " \n" +
                "Signature : " + Arrays.toString(signature) + " \n" +
                "Hash : " + getHash() + " \n" +
                "Inputs : " + inputs + " \n" +
                "Outputs : " + outputs + " \n";
    }

}
