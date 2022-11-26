package io.mybits.utils;

public class Constants {

    public static String BLOCKCHAIN_HOST = "";

    public static int WALLET_PORT = 0, WALLET_FALLBACK = 1, BLOCKS = 2, BLOCKS_FALLBACK = 3, CONTRACTS = 4, CONTRACTS_FALLBACK = 5; // Ports
    public static int TRANSACTION_FAST = 0, TRANSACTION_FALLBACK_FAST = 1, DEV_FAST = 2, DEV_FALLBACK_FAST = 3; // Fast Ports

    public static int MAIN_NETWORK_THREAD = 0, TCP_THREAD = 1, UDP_THREAD = 2, FAST_TCP_THREAD = 3, FALLBACK_THREAD = 4, QUEUE = 5; // Network Threads

    public static final int[] ports = new int[]{
            33322, //Dev Port
            33324, //Dev Port Callback
    };

    /**
     * Fast Ports (Used for creation calls)
     */

    public static final int[] fastPorts = new int[]{
            33306, //Wallet
            33310 //Transactions
    };

    /**
     * Client UDP Ports (Used for calls to chain for faster processing)
     */

    public static final int[] clientPorts = new int[]{
            33307, //Transactions
            33309, //Transaction Callback
            33311, //Wallets
            33313, //Wallet Callback
            33315, //Blocks
            33317, //Blocks Callback
            33319, //Contract
            33321,  //Contract Callback

    };
}
