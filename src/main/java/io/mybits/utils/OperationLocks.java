package io.mybits.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class OperationLocks {

    private final AtomicBoolean[] locks = new AtomicBoolean[16];

    public OperationLocks(){
        for(int i = 0; i < locks.length; i++){
            locks[i] = new AtomicBoolean(false);
        }
    }

    public void setLock(int index){
        locks[index].set(true);
    }

    public void unlock(int index){
        locks[index].set(false);
    }

    public boolean isLocked(int index){
        return locks[index].get();
    }

}
