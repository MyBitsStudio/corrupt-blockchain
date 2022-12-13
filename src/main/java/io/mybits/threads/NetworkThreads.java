package io.mybits.threads;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NetworkThreads {

    private final ThreadFactory factory;
    private ScheduledThreadPoolExecutor executor;

    public NetworkThreads(){
            ThreadFactoryBuilder builder = new ThreadFactoryBuilder()
                    .setNameFormat("Network-Thread-%d")
                    .setThreadFactory(new BlockChainThreadFactory());
            this.factory = builder.build();
            buildExecutor();
    }

    public void addToFactory(Runnable run){
            this.factory.newThread(run).start();
    }

    protected void buildExecutor(){
        executor = new ScheduledThreadPoolExecutor(1, factory);
    }

    public void addToExecutor(Runnable run, int seconds){
        this.executor.schedule(run, seconds, TimeUnit.SECONDS);
    }
    public void scheduleFixedRate(Runnable run, int seconds, int delay){
        this.executor.scheduleAtFixedRate(run, delay, seconds, TimeUnit.MILLISECONDS);
    }



}
