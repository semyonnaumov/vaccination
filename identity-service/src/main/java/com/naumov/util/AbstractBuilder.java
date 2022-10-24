package com.naumov.util;

import java.util.function.Supplier;

public abstract class AbstractBuilder<T> {
    private final T instance;
    private boolean isDone;

    protected AbstractBuilder(Supplier<T> instanceConstructor) {
        this.instance = instanceConstructor.get();
        this.isDone = false;
    }

    protected T getInstance() {
        return this.instance;
    }

    protected void checkDone() {
        if (this.isDone) throw new IllegalStateException("This builder has been used already");
    }

    public T build() {
        checkDone();
        this.isDone = true;
        return this.instance;
    }
}
