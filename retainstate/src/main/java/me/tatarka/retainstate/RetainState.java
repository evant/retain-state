package me.tatarka.retainstate;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.SparseArray;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RetainState implements Iterable<Object> {
    /**
     * Attempts to get the retain state for the given Activity with the given context. For this to
     * work, the context <em>must</em> be the Activity or a {@link ContextWrapper} around it, and
     * that Activity <em>must</em> implement {@link Provider}.
     */
    public static RetainState get(Context context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        if (context instanceof Provider) {
            return ((Provider) context).getRetainState();
        }
        if (context instanceof ContextWrapper) {
            return get(((ContextWrapper) context).getBaseContext());
        }
        throw new IllegalArgumentException("Given context " + context + " does not implement RetainState.Provider");
    }

    private SparseArray<Object> state;

    /**
     * Constructs a new instance with the given saved state. This state should be obtained from
     * {@link #getState()} and saved across configuration changes.
     */
    @SuppressWarnings("unchecked")
    public RetainState(Object retainedState) {
        if (retainedState != null) {
            state = (SparseArray<Object>) retainedState;
        }
    }

    /**
     * Returns the state to save across configuration changes.
     */
    public Object getState() {
        return state;
    }

    /**
     * Get an object that will survive configuration changes, creating it if it doesn't yet exist.
     * The given id <em>must</em> be unique for the current Activity. You can use {@code R.id} to
     * create unique id's.
     */
    @SuppressWarnings("unchecked")
    public <T> T retain(int id, OnCreate<T> onCreate) {
        if (state == null) {
            state = new SparseArray<>();
        }
        T item = (T) state.get(id);
        if (item == null) {
            item = onCreate.onCreate();
            state.put(id, item);
        }
        return item;
    }

    /**
     * Get an existing object with the given id. Returns null if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int id) {
        if (state == null) {
            return null;
        }
        return (T) state.get(id);
    }

    /**
     * Removes the object with the given id and returns it.
     */
    @SuppressWarnings("unchecked")
    public <T> T remove(int id) {
        if (state != null) {
            Object value = state.get(id);
            state.remove(id);
            return (T) value;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Object> iterator() {
        if (state == null) {
            return (Iterator<Object>) EMPTY_ITERATOR;
        } else {
            return new RetainStateIterator(state);
        }
    }

    public interface Provider {
        RetainState getRetainState();
    }

    public interface OnCreate<T> {
        T onCreate();
    }

    private static class RetainStateIterator implements Iterator<Object> {
        private final SparseArray<Object> state;
        private int index;

        private RetainStateIterator(SparseArray<Object> state) {
            this.state = state;
        }

        @Override
        public boolean hasNext() {
            return index < state.size();
        }

        @Override
        public Object next() {
            return state.valueAt(index++);
        }

        @Override
        public void remove() {
            state.remove(index);
        }
    }

    /**
     * Because {@link Collections#emptyIterator()} wasn't added until api 19.
     */
    private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }
    };
}
