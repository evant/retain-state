package me.tatarka.loader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

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
    private static final int STATE_DESTROYED = 1 << 3;

    private static final int CALLBACKS_START = 1;
    private static final int CALLBACKS_RESULT = 2;
    private static final int CALLBACKS_COMPLETE = 4;

    private static boolean isRunning(int state) {
        return (state & STATE_RUNNING) == STATE_RUNNING;
    }
    
    private static boolean isCompleted(int state) {
        return (state & STATE_COMPLETED) == STATE_COMPLETED;
    }

    /**
     * Throws an {@link IllegalStateException} if loader is destroyed.
     */
    private static void checkDestroyed(String method, int state) {
        if ((state & STATE_DESTROYED) == STATE_DESTROYED) {
            throw new IllegalStateException("cannot call " + method + "() after destroy()");
        }
    }

    @Nullable
    private Callbacks<T> callbacks;
    @Nullable
    private Receiver receiver;
    private T cachedResult;
    private AtomicInteger state = new AtomicInteger();

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int callback = msg.arg1;
            @SuppressWarnings("unchecked")
            HandlerArgs<Object> args = (HandlerArgs<Object>) msg.obj;
            if ((callback & CALLBACKS_RESULT) == CALLBACKS_RESULT) {
                args.callbacks.onLoaderResult(args.cachedResult);
            } else if ((callback & CALLBACKS_START) == CALLBACKS_START) {
                args.callbacks.onLoaderStart();
            }
            if ((callback & CALLBACKS_COMPLETE) == CALLBACKS_COMPLETE) {
                args.callbacks.onLoaderComplete();
            }
        }
    };

    /**
     * Starts the loader if it has not already been started, calling {@link #onStart(Receiver)}} and
     * triggering {@link Callbacks#onLoaderStart()}. This must be called on the main thread.
     */
    @MainThread
    public final void start() {
        int s = state.get();
        checkDestroyed("start", s);
        if (!isRunning(s) && !isCompleted(s)) {
            state.set(STATE_RUNNING);
            if (callbacks != null) {
                callbacks.onLoaderStart();
            }
            receiver = new Receiver();
            receiver.myState |= Receiver.SYNCHRONOUS;
            onStart(receiver);
            receiver.myState &= ~Receiver.SYNCHRONOUS;
        }
    }

    /**
     * Cancels the loader if it's running, calling {@link #onCancel()} and removing any cached data.
     * This must be called on the main thread.
     */
    @MainThread
    public final void cancel() {
        int s = state.get();
        checkDestroyed("cancel", s);
        cachedResult = null;
        if (receiver != null) {
            receiver.myState = Receiver.CANCELED;
            receiver = null;
        }
        handler.removeMessages(0);
        if (isRunning(s)) {
            onCancel();
        }
        state.set(0);
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
     * Destroys the loader, canceling it if necessary. This is normally called for you by {@link
     * LoaderManager}. This will trigger {@link #onDestroy()} allowing you to clean up any resources
     * if necessary. You should not call any other methods on the loader after this.
     */
    @MainThread
    public final void destroy() {
        int s = state.get();
        checkDestroyed("destroy", s);
        cancel();
        state.set(STATE_DESTROYED);
        callbacks = null;
        onDestroy();
    }

    /**
     * Returns true if the loader is running. That is, if it has been started and not stopped and
     * {@link Receiver#complete()} has not been called. If this is true than you may expect one or
     * more results to be delivered.
     */
    public final boolean isRunning() {
        return isRunning(state.get());
    }

    /**
     * Returns true if the loader has a result. If it does, the result will be delivered immediately
     * after the loader has been re-attached.
     */
    public final boolean hasResult() {
        return (state.get() & STATE_HAS_RESULT) == STATE_HAS_RESULT;
    }

    /**
     * Returns true if the loader has been completed. If it has, then it has been started but no
     * more results may be delivered and it's no longer running.
     */
    public final boolean isCompleted() {
        return isCompleted(state.get());
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
     * Optionally clean up any resources the loader is using when it is destroyed. No methods will
     * be called on the loader after this.
     */
    protected void onDestroy() {
    }

    /**
     * Set the callbacks for the loader. This is normally called for you by {@link LoaderManager}.
     * Data will be delivered of if the loader already has it. Otherwise, {@link
     * Callbacks#onLoaderStart()} will be called to give you the opportunity to show any loading ui.
     * You may pass in null to clear the callbacks. This must be called on the main thread.
     */
    @MainThread
    public final void setCallbacks(@Nullable final Callbacks<T> callbacks) {
        this.callbacks = callbacks;
        handler.removeMessages(0);
        if (callbacks != null) {
            int methods = 0;
            if (hasResult()) {
                methods |= CALLBACKS_RESULT;
            } else if (isRunning()) {
                methods |= CALLBACKS_START;
            }
            if (isCompleted()) {
                methods |= CALLBACKS_COMPLETE;
            }
            dispatchCallbacks(callbacks, methods);
        }
    }

    /**
     * So that callback methods are consistently async, we post them to a handler.
     */
    private void dispatchCallbacks(Callbacks<T> callbacks, int methods) {
        Message message = handler.obtainMessage(0);
        message.obj = new HandlerArgs<>(cachedResult, callbacks);
        message.arg1 = methods;
        handler.dispatchMessage(message);
    }

    /**
     * Receives results from the loader and notifies the loader's callbacks.
     */
    public final class Receiver {
        /**
         * If the receiver is canceled, ignore any delivered results.
         */
        private static final int CANCELED = 1;
        /**
         * If the receiver is complete, any calls to {@link #deliverResult(Object)} is an error.
         */
        private static final int COMPLETE = 2;
        /**
         * It's possible that a result is immediately delivered inside {@link
         * #onStart(Loader.Receiver)}. Because we don't want to surprise our consumer with immediate
         * results, we should post them to a handler in this case.
         */
        private static final int SYNCHRONOUS = 4;

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

            int s = state.get();
            state.set(s | STATE_HAS_RESULT);

            cachedResult = result;
            if (callbacks != null) {
                if ((myState & SYNCHRONOUS) == SYNCHRONOUS) {
                    dispatchCallbacks(callbacks, CALLBACKS_RESULT);
                } else {
                    callbacks.onLoaderResult(result);
                }
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

            int s = state.get();
            state.set((s & ~STATE_RUNNING) | STATE_COMPLETED);

            if (callbacks != null) {
                if ((myState & SYNCHRONOUS) == SYNCHRONOUS) {
                    dispatchCallbacks(callbacks, CALLBACKS_COMPLETE);
                } else {
                    callbacks.onLoaderComplete();
                }
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

    private static final class HandlerArgs<T> {
        final T cachedResult;
        final Callbacks<T> callbacks;

        private HandlerArgs(T cachedResult, Callbacks<T> callbacks) {
            this.cachedResult = cachedResult;
            this.callbacks = callbacks;
        }
    }
}
