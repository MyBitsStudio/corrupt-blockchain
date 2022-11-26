package io.mybits;

import io.mybits.hyperledger.HyperLedger;
import io.mybits.network.Network;
import io.mybits.network.NetworkConstants;
import io.mybits.utils.LedgerLogs;

public class Launcher {

    public static Network network;

    public static void main(String[] args) {

        LedgerLogs.logBooting("HyperLedger is starting!", LedgerLogs.INFO, true);

        LedgerLogs.logBooting("Network Constants starting!", LedgerLogs.INFO, true);

        NetworkConstants.initKeys();

        LedgerLogs.logBooting("Network Constants are successful!", LedgerLogs.SUCCESS, true);

        LedgerLogs.logBooting("HyperLedger is starting!", LedgerLogs.INFO, true);

        HyperLedger.singleton().start();

        LedgerLogs.logBooting("HyperLedger has loaded!", LedgerLogs.SUCCESS, true);

        LedgerLogs.logBooting("Network is starting!", LedgerLogs.INFO, true);

        network = Network.singleton();
        network.start();
        while(network.isBooting()){}

        LedgerLogs.logBooting("Network is successful!", LedgerLogs.SUCCESS, true);

        LedgerLogs.logBooting("Running network later requests...", LedgerLogs.INFO, true);

        network.runLater();

        LedgerLogs.logBooting("Hyperledger is running and waiting requests...", LedgerLogs.INFO, true);


    }
}
