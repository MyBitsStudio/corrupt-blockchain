package io.mybits.utils;


import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LedgerLogs {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static String BOOT_UP = "data/logs/bootLog.txt";
    public static String PRINT_ALL = "data/logs/print/";
    public static String CHAIN_LOG = "data/logs/chain.txt";
    public static String PACKET_LOG = "data/logs/packet.txt";
    public static String TCP_LOG = "data/logs/tcp.txt";
    public static String PDETAIL_LOG = "data/logs/packet_detail.txt";

    public static int ERROR = 0, WARNING = 1, INFO = 2, DEBUG = 3, TRACE = 4, SUCCESS = 5; //LogType

   //  LogHelper.logChain("Funds not available "+this.publicAddress+":"+value, LogHelper.ERROR, true, "");

    public static void log(String log, int logType){
        switch(logType){
            case 0 -> System.out.println(ANSI_RED + getTime() + " - [ERROR] " + log + ANSI_RESET);
            case 1 -> System.out.println(ANSI_YELLOW + getTime() + " - [WARNING] " + log + ANSI_RESET);
            case 2 -> System.out.println(ANSI_BLUE + getTime() + " - [INFO] " + log + ANSI_RESET);
            case 3 -> System.out.println(ANSI_CYAN + getTime() + " - [DEBUG] " + log + ANSI_RESET);
            case 4 -> System.out.println(ANSI_PURPLE + getTime() + " - [TRACE] " + log + ANSI_RESET);
            case 5 -> System.out.println(ANSI_GREEN + getTime() + " - [SUCCESS] " + log + ANSI_RESET);
        }
    }

    public static void logBooting(String log, int logType, boolean print){
        if(print)
            log(log, logType);
        write(log, BOOT_UP);
    }

    public static void logPrintAll(String log, int logType, boolean print, String... info){
        if(print)
            log(log, logType);
        write(log, PRINT_ALL+info[0]+".txt");
    }

    public static void logChain(String log, int logType, boolean print){
        if(print)
            log(log, logType);
        write(log, CHAIN_LOG);
    }

    public static void logPacket(String log, int logType, boolean print){
        if(print)
            log(log, logType);
        write(log, PACKET_LOG);
    }

    public static void logTCP(String log, int logType, boolean print){
        if(print)
            log(log, logType);
        write(log, TCP_LOG);
    }

    public static void logPacketDetail(String log, int logType, boolean print){
        if(print)
            log(log, logType);
        write(log, PDETAIL_LOG);
    }

    public static void write(String message, String aFileName){
        try {
            FileWriter fw = new FileWriter(aFileName, true);
            fw.write(getTime() + "" + message + "\t");
            fw.write(System.lineSeparator());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static @NotNull String getTime() {
        Date getDate = new Date();
        String timeFormat = "M/d/yy hh:mma";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
        return "[" + sdf.format(getDate) + "] --- ";
    }
}
