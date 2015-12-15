package me.tatarka.loader;

import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

/**
 * A loader helps connect async operations to your views. It is retained across configuration
 * changes with {@link LoaderManager}. You run your operation in {@link #onStart()} and deliver the
 * result with {@link #deliverResult(Object)}. Note that {@link #onStart()} is <em>not</em> run in a
 * background thread. You should handle threading yourself with an {@link android.os.AsyncTask} or
 * other mechanism. The result will be cached and re-delivered after a configuration change. You may
 * also optionally implement {@link #onStop()} if you can cancel your work when it is no longer
 * needed.
 *
 * @param <T> The type of result that the loader will deliver
 */
public abstract class Loader<T> {
    private static final int STATE_RUNNING = 1;
    private static final int STATE_HAS_RESULT = 1 << 1;
    private static final int STATE_COMPLETED = 1 << 2;

    @Nullable
    private Callbacks<T> callbacks;
    private T cachedResult;
    private int state;

    /**
     * Starts the loader if it's not already running, calling {@link #onStart()} and triggering
     * {@link Callbacks#onLoaderStart()}. This must be called on the main thread.
     */
    @MainThread
    public final void start() {
        if (!isRunning()) {
            state = STATE_RUNNING;
            if (callbacks != null) {
                callbacks.onLoaderStart();
            }
            onStart();
        }
    }

    /**
     * Stops the loader if it's running, calling {@link #onStop()}. This must be called on the main
     * thread.
     */
    @MainThread
    public final void stop() {
        cachedResult = null;
        if (isRunning()) {
            onStop();
        }
        state = 0;
    }

    /**
     * Forces the loader to restart. This is a convenience method for calling {@link #stop()}
     * followed by {@link #start()}. This must be called on the main thread.
     */
    @MainThread
    public final void restart() {
        stop();
        start();
    }

    /**
     * Returns true if the loader is running. That is, if it has been started and not stopped and
     * {@link #complete()} has not been called. If this is true than you may expect one or more
     * results to be delivered.
     */
    public final boolean isRunning() {
        return (state & STATE_RUNNING) == STATE_RUNNING;
    }

    /**
     * Returns true if the loader has a result. If it does, the result will be delivered immediately
     * after the loader has been re-attached.
     */
    public final boolean hasResult() {
        return (state & STATE_HAS_RESULT) == STATE_HAS_RESULT;
    }

    /**
     * Returns true if the loader has been completed. If it has, then it has been started but no
     * more results may be delivered and it's no longer running.
     */
    public final boolean isCompleted() {
        return (state & STATE_COMPLETED) == STATE_COMPLETED;
    }

    /**
     * Do your loader work. This is run on the main thread so you are expected to handle threading
     * yourself either by using an {@link android.os.AsyncTask} or other mechanism. When you have
     * one or more results you should call {@link #deliverResult(Object)} and then call {@link
     * #complete()} when you are done.
     */
    protected abstract void onStart();

    /**
     * Optionally stop doing work because the result is no longer needed. This will only be called
     * if the loader has been started and is running. This is run on the main thread.
     */
    protected void onStop() {

    }

    /**
     * Deliver a result to {@link Callbacks#onLoaderResult(Object)}. When you are done delivering
     * results you should call {@link #complete()}. This must be run on the main thread.
     */
    @MainThread
    protected final void deliverResult(T result) {
        state |= STATE_HAS_RESULT;
        cachedResult = result;
        if (callbacks != null) {
            callbacks.onLoaderResult(result);
        }
    }

    /**
     * Marks the loader as complete and triggers {@link Callbacks#onLoaderComplete()}.
     */
    @MainThread
    protected final void complete() {
        state &= ~STATE_RUNNING;
        state |= STATE_COMPLETED;
        if (callbacks != null) {
            callbacks.onLoaderComplete();
        }
    }

    @MainThread
    final void setCallbacks(@Nullable Callbacks<T> callbacks) {
        this.callbacks = callbacks;
        if (callbacks != null) {
            if (hasResult()) {
                callbacks.onLoaderResult(cachedResult);
            } else if (isRunning()) {
                callbacks.onLoaderStart();
            }
            if (isCompleted()) {
                callbacks.onLoaderComplete();
            }
        }
    }

    /**
     * Implement this callback to listen to data from the loader.
     */
    public interface Callbacks<T> {
        /**
         * Called when the loader is started and when the loader is running but does not yet have a
         * result when the callback is attached.
         */
        void onLoaderStart();

        /**
         * Called when the loader delivers a result and with the last result when the callback is
         * attached if it exists.
         */
        void onLoaderResult(T result);

        /**
         * Called when the loader is complete and will deliver no more results and when the loader
         * was completed when the callback is attached.
         */
        void onLoaderComplete();
    }

    public static abstract class CallbacksAdapter<T> implements Callbacks<T> {
        @Override
        public void onLoaderStart() {

        }

        @Override
        public void onLoaderResult(T result) {

        }

        @Override
        public void onLoaderComplete() {

        }
    }
}
