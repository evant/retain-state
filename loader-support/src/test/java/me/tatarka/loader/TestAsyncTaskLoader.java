package me.tatarka.loader;

import android.support.v4.os.OperationCanceledException;

import java.util.concurrent.Executor;

import me.tatarka.retainstate.RetainState;

public class TestAsyncTaskLoader<T> extends AsyncTaskLoader<T> {

    public static <T> RetainState.OnCreate<TestAsyncTaskLoader<T>> create() {
        return new RetainState.OnCreate<TestAsyncTaskLoader<T>>() {
            @Override
            public TestAsyncTaskLoader<T> onCreate() {
                return new TestAsyncTaskLoader<>();
            }
        };
    }

    public TestAsyncTaskLoader() {
        super();
    }

    public TestAsyncTaskLoader(Executor executor) {
        super(executor);
    }

    private T result;
    private boolean throwOperationCanceled;

    @Override
    protected T doInBackground() {
        if (throwOperationCanceled) {
            throw new OperationCanceledException();
        } else {
            return result;
        }
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void throwCanceledException() {
        this.throwOperationCanceled = true;
    }
}
