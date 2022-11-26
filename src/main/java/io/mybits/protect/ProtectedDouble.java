package io.mybits.protect;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

public class ProtectedDouble implements Serializable {

    @Serial
    private static final long serialVersionUID = 691729711955544720L;
    volatile AtomicReference<Double> value = new AtomicReference<>(0.000000);

    public ProtectedDouble() {
    }

    public ProtectedDouble(double value) {
        this.value.set(value);
    }

    public double get() {
        synchronized (this) {
            return value.get();
        }
    }

    public double add(double value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v + value);
        }
    }

    public double subtract(double value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v - value);
        }
    }

    public double multiply(double value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v * value);
        }
    }

    public double divide(double value) {
        synchronized (this) {
            return this.value.updateAndGet(v -> v / value);
        }
    }

    public double increment() {
        synchronized (this) {
            return this.value.updateAndGet(v -> v + 1);
        }
    }
}
