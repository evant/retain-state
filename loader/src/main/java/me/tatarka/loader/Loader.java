package me.tatarka.loader;

import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

/**
 * A loader helps connect async operations to your views. It is retained across configuration
 * changes with {@link LoaderManager}. You run your operation in {@link #onStart(Receiver)}} and
 * deliver the result with {@link Receiver#deliverResult(Object)}. Note that {@link
 * #onStart(Receiver)}} is <em>not</em> run in a background thread. You should handle threading
 * yourself with an {@link android.os.AsyncTask} or other mechanism. The result will be cached and
 * re-delivered after a configuration change. You may also optionally implement {@link #onCancel()}
 * if you can cancel your work when it is no longer needed.
 *
 * @param <T> The type of result that the loader will deliver
 */
public abstract class Loader<T> {
    private static final int STATE_RUNNING = 1;
    private static final int STATE_HAS_RESULT = 1 << 1;
    private static final int STATE_COMPLETED = 1 << 2;

    @Nullable
    private Callbacks<T> callbacks;
    @Nullable
    private Receiver receiver;
    private T cachedResult;
    private int state;

    /**
     * Starts the loader if it's not already running, calling {@link #onStart(Receiver)}} and
     * triggering {@link Callbacks#onLoaderStart()}. This must be called on the main thread.
     */
    @MainThread
    public final void start() {
        if (!isRunning()) {
            state = STATE_RUNNING;
            if (callbacks != null) {
                callbacks.onLoaderStart();
            }
            receiver = new Receiver();
            onStart(receiver);
        }
    }

    /**
     * Cancels the loader if it's running, calling {@link #onCancel()} and removing any cached data.
     * This must be called on the main thread.
     */
    @MainThread
    public final void cancel() {
        cachedResult = null;
        if (receiver != null) {
            receiver.myState = Receiver.CANCELED;
            receiver = null;
        }
        if (isRunning()) {
            onCancel();
        }
        state = 0;
    }

    /**
     * Forces the loader to restart. This is a convenience method for calling {@link #cancel()}
     * followed by {@link #start()}. This must be called on the main thread.
     */
    @MainThread
    public final void restart() {
        cancel();
        start();
    }

    /**
     * Returns true if the loader is running. That is, if it has been started and not stopped and
     * {@link Receiver#complete()} has not been called. If this is true than you may expect one or
     * more results to be delivered.
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
     * Returns true if the loader has non-null callbacks attached with {@link
     * #setCallbacks(Callbacks)}.
     */
    public final boolean isAttached() {
        return callbacks != null;
    }

    /**
     * Do your loader work. This is run on the main thread so you are expected to handle threading
     * yourself either by using an {@link android.os.AsyncTask} or other mechanism. When you have
     * one or more results you should call {@link Receiver#deliverResult(Object)} and then call
     * {@link Receiver#complete()} when you are done.
     */
    protected abstract void onStart(final Receiver receiver);

    /**
     * Optionally cancel doing work because the result is no longer needed. This will only be called
     * if the loader has been started and is running. This is run on the main thread.
     */
    protected void onCancel() {
    }

    /**
     * Set the callbacks for the loader. Data will be immediately delivered at this point of if the
     * loader already has it. Otherwise, {@link Callbacks#onLoaderStart()} will be called to give
     * you the opportunity to show any loading ui. You may pass in null to clear the callbacks. This
     * must be called on the main thread.
     */
    @MainThread
    public final void setCallbacks(@Nullable Callbacks<T> callbacks) {
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
     * Receives results from the loader and notifies the loader's callbacks.
     */
    public class Receiver {
        private static final int CANCELED = 1;
        private static final int COMPLETE = 2;

        private int myState = 0;

        /**
         * Deliver a result to {@link Callbacks#onLoaderResult(Object)}. When you are done
         * delivering results you should call {@link #complete()}. If the loader has already been
         * canceled, then the result is ignored as it is not expected to be used. This must be run
         * on the main thread.
         */
        @MainThread
        public final void deliverResult(T result) {
            if ((myState & CANCELED) == CANCELED) {
                return;
            }
            if ((myState & COMPLETE) == COMPLETE) {
                throw new IllegalStateException("cannot deliver result after complete()");
            }
            state |= STATE_HAS_RESULT;
            cachedResult = result;
            if (callbacks != null) {
                callbacks.onLoaderResult(result);
            }
        }

        /**
         * Marks the loader as complete and triggers {@link Callbacks#onLoaderComplete()} If the
         * loader has already been canceled then the call will be ignored.
         */
        @MainThread
        public final void complete() {
            if ((myState & CANCELED) == CANCELED) {
                return;
            }
            if ((myState & COMPLETE) == COMPLETE) {
                throw new IllegalStateException("complete() already called");
            }
            myState = COMPLETE;
            state &= ~STATE_RUNNING;
            state |= STATE_COMPLETED;
            if (callbacks != null) {
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
