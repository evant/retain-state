package me.tatarka.loader;

import android.os.AsyncTask;
import android.support.v4.os.OperationCanceledException;

import java.util.concurrent.Executor;

/**
 * A {@link Loader} that runs your some work in an {@link AsyncTask} similar to {@link
 * android.content.AsyncTaskLoader}.
 */
public abstract class AsyncTaskLoader<T> extends Loader<T> {

    private final Executor executor;
    private AsyncTask<Void, Void, T> task;

    public AsyncTaskLoader() {
        this.executor = AsyncTask.THREAD_POOL_EXECUTOR;
    }

    public AsyncTaskLoader(Executor executor) {
        this.executor = executor;
    }

    /**
     * Called on a worker thread to perform the load and return the result. To support cancellation,
     * this method should periodically check {@link #isRunning()} and return early or throw an
     * {@link android.support.v4.os.OperationCanceledException} if it returns false.
     */
    protected abstract T doInBackground();

    @Override
    protected final void onStart(final Receiver receiver) {
        task = new AsyncTask<Void, Void, T>() {
            @Override
            protected T doInBackground(Void... params) {
                try {
                    return AsyncTaskLoader.this.doInBackground();
                } catch (OperationCanceledException e) {
                    if (!isRunning()) {
                        return null;
                    } else {
                        // Thrown when not actually canceled, just propagate exception.
                        throw e;
                    }
                }
            }

            @Override
            protected void onPostExecute(T value) {
                receiver.deliverResult(value);
                receiver.complete();
            }
        };
        task.executeOnExecutor(executor);
    }

    @Override
    protected final void onCancel() {
        task.cancel(false);
        task = null;
    }
}
