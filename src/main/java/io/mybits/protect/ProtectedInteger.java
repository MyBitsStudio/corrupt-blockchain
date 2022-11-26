package io.mybits.protect;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtectedInteger implements Serializable {

    @Serial
    private static final long serialVersionUID = 1741291123549435957L;
    volatile AtomicInteger value = new AtomicInteger(0);

    public ProtectedInteger() {
    }

    public ProtectedInteger(int value) {
        this.value.set(value);
    }

    public int get() {
        synchronized (this) {
            return value.get();
        }
    }

    public int add(int value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v + value);
        }
    }

    public int subtract(int value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v - value);
        }
    }

    public int multiply(int value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v * value);
        }
    }

    public int divide(int value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v / value);
        }
    }

    public int increment() {
        synchronized (this) {
            return this.value.updateAndGet(v -> v + 1);
        }
    }
}
