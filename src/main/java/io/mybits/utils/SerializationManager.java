package io.mybits.utils;

import io.mybits.hyperledger.block.Block;
import io.mybits.hyperledger.contract.Contract;
import io.mybits.hyperledger.wallet.Wallet;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class SerializationManager {

    public static final String BLOCKS = "data/master/chain/blocks/";
    public static final String WALLETS = "data/master/chain/wallet/";
    public static final String CONTRACTS = "data/master/chain/contract/";

    public static void storeSerializableClass(Serializable o, File f)
            throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(o);
        out.flush();
        out.close();
    }

    public static Object loadSerializedFile(File f) throws IOException,
            ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);
        Object object = in.readObject();
        fileInputStream.close();
        in.close();
        return object;
    }

    public static void serializeBlock(@NotNull Block block){
        try{
            synchronized (BLOCKS){
                storeSerializableClass(block, new File(BLOCKS + block.getChainLink() + ".block"));
            }
        }catch(IOException i){
            i.printStackTrace();
        }
    }

    public static Block loadBlock(@NotNull File file){
        Block block = null;
        if(file.exists()){
            try{
                block = (Block) loadSerializedFile(file);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return block;
    }

    public static void serializeContract(@NotNull Contract contract){
        try{
            synchronized (CONTRACTS){
                storeSerializableClass(contract, new File(CONTRACTS + contract.getAddress() + ".contract"));
            }
        }catch(IOException i){
            i.printStackTrace();
        }
    }

    public static Contract loadContract(@NotNull File file){
        Contract contract = null;
        if(file.exists()){
            try{
                contract = (Contract) loadSerializedFile(file);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return contract;
    }

    public static void serializeWallet(@NotNull Wallet wallet){
        try{
            synchronized (WALLETS){
                storeSerializableClass(wallet, new File(WALLETS + wallet.getPublicAddress() + ".wallet"));
            }
        }catch(IOException i){
            i.printStackTrace();
        }
    }

    public static Wallet loadWallet(@NotNull File file){
        Wallet wallet = null;
        if(file.exists()){
            try{
                wallet = (Wallet) loadSerializedFile(file);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return wallet;
    }
}
