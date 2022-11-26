package io.mybits.services;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.hyperledger.block.Block;
import io.mybits.utils.SerializationManager;

public class BlockGenerator {

    private final HyperLedger hyperLedger;

    public BlockGenerator(HyperLedger ledger){
        this.hyperLedger = ledger;
        startGenerator();
    }

    private void startGenerator() {
        hyperLedger.getThreads().scheduleFixedRate(() -> {
            double reward = (2 + (hyperLedger.getChain().size() + Math.pow(hyperLedger.getChain().size(), 1.6f)));
            //LogHelper.logChain("Balance : "+String.format("%.6f", reward), LogHelper.DEBUG, true);
            Block block = new Block(hyperLedger.getChain().get(hyperLedger.getChain().size() - 1), Double.parseDouble(String.format("%.6f", reward)));
            hyperLedger.getChain().add(block);
            SerializationManager.serializeBlock(block);
            System.out.println(block);
            Block block2 = hyperLedger.getChain().get(hyperLedger.getChain().size() - 2).blockDigest();
            SerializationManager.serializeBlock(block2);
            if(hyperLedger.getChain().size() >= 11){
                hyperLedger.getThreads().addToFactory(() -> {
                    Block inactive = hyperLedger.getChain().get(hyperLedger.getChain().size() - 10);
                    inactive.setStage("INACTIVE");
//                    LogHelper.logChain("Inactive block: "+inactive.getHash(), LogHelper.DEBUG, true);
//                    LogHelper.logChain("Balance left : "+inactive.getBalance(), LogHelper.DEBUG, true);
                    SerializationManager.serializeBlock(inactive);
                });
            }
        }, 300, 300);
    }
}
