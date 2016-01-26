package me.tatarka.loader;

import me.tatarka.retainstate.RetainState;

public class TestLoader<T> extends Loader<T> {
    public static <T> RetainState.OnCreate<TestLoader<T>> create() {
        return new RetainState.OnCreate<TestLoader<T>>() {
            @Override
            public TestLoader<T> onCreate() {
                return new TestLoader<>();
            }
        };
    }

    private Receiver receiver;
    private boolean isCanceled;
    
    @Override
    protected void onStart(Receiver receiver) {
        this.receiver = receiver;
        isCanceled = false;
    }

    @Override
    protected void onCancel() {
        isCanceled = true;
    }

    public void deliverResult(T result) {
        if (receiver == null) {
            throw new IllegalStateException("Loader isn't running");
        }
        receiver.deliverResult(result);
    }

    public void complete() {
        if (receiver == null) {
            throw new IllegalStateException("Loader isn't running");
        }
        receiver.complete();
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
