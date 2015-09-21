package me.tatarka.retainstate;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;

public class RetainState {
    /**
     * Attempts to get the retain state for the given Activity with the given context. For this to
     * work, the context <em>must</em> by the Activity or a {@link ContextWrapper} around it, and
     * that Activity <em>must</em> implement {@link me.tatarka.retainstate.RetainState.RetainStateProvider}.
     */
    public static RetainState get(Context context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        if (context instanceof RetainStateProvider) {
            return ((RetainStateProvider) context).getRetainState();
        }
        if (context instanceof ContextWrapper) {
            return get(((ContextWrapper) context).getBaseContext());
        }
        throw new IllegalArgumentException("Given context " + context + " does not implement RetainStateProvider");
    }

    private final SparseArray<Object> state;

    /**
     * Constructs a new instance with the state saved by the given {@link FragmentActivity}.
     */
    public RetainState(FragmentActivity activity) {
        this(activity.getLastCustomNonConfigurationInstance());
    }

    /**
     * Constructs a new instance with the state saved by the given {@link Activity}.
     */
    public RetainState(Activity activity) {
        this(activity.getLastNonConfigurationInstance());
    }

    /**
     * Constructs a new instance with the given saved state. This state should be obtained from
     * {@link #retainState()} and saved across configuration changes.
     */
    @SuppressWarnings("unchecked")
    public RetainState(@Nullable Object retainedState) {
        if (retainedState != null) {
            state = (SparseArray<Object>) retainedState;
        } else {
            state = new SparseArray<>();
        }
    }

    /**
     * Returns the state to save across configuration changes.
     */
    public Object retainState() {
        return state;
    }

    /**
     * Get an object that will survive configuration changes, creating it if it doesn't yet exist.
     * The given id <em>must</em> be unique for the current Activity. You can use {@code R.id} to
     * create unique id's.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int id, OnCreate<T> onCreate) {
        T item = (T) state.get(id);
        if (item == null) {
            item = onCreate.onCreate();
            state.put(id, item);
        }
        return item;
    }

    public interface RetainStateProvider {
        RetainState getRetainState();
    }

    public interface OnCreate<T> {
        T onCreate();
    }
}
