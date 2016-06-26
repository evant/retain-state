package me.tatarka.retainstate;

import android.content.ContextWrapper;
import android.util.SparseArray;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RetainState implements Iterable<Object> {

    /**
     * Convenience {@link OnCreate} for when you want to store a {@code RetainState} in a {@code
     * RetainState} which is useful for nesting.
     */
    public static final OnCreate<RetainState> CREATE = new OnCreate<RetainState>() {
        @Override
        public RetainState onCreate() {
            return new RetainState(null);
        }
    };

    /**
     * Attempts to get the retain state for the given host. For this to work, the host <em>must</em>
     * implement {@link Provider} or be a {@link ContextWrapper} around it.
     */
    public static RetainState from(Object host) {
        if (host == null) {
            throw new NullPointerException("context == null");
        }
        if (host instanceof Provider) {
            return ((Provider) host).getRetainState();
        }
        if (host instanceof ContextWrapper) {
            return from(((ContextWrapper) host).getBaseContext());
        }
        throw new IllegalArgumentException("Given host " + host + " does not implement RetainState.Provider");
    }

    private SparseArray<Object> state;
    private boolean isRetaining;

    /**
     * Constructs a new instance with the given saved state. This state should be obtained from
     * {@link #onRetain()} and saved across configuration changes.
     */
    @SuppressWarnings("unchecked")
    public RetainState(Object retainedState) {
        if (retainedState != null) {
            state = (SparseArray<Object>) retainedState;
            setIsRetaining(false);
        }
    }

    /**
     * Notify that the RetainState is about to retained and return the state that should be
     * retained.
     */
    public Object onRetain() {
        setIsRetaining(true);
        return state;
    }

    /**
     * Returns if the RetainState is about to be preserved across a configuration change. This is
     * useful for lifecycle-aware components that may want to clean up resources when it will no
     * longer be retained. This will be true after {@link #onRetain()} is called.
     */
    public boolean isRetaining() {
        return isRetaining;
    }

    /**
     * Notifies the RetainState that it will no longer be retained. Will remove any retained state.
     */
    public void destroy() {
        setIsRetaining(false);
        if (state != null) {
            state.clear();
        }
    }

    private void setIsRetaining(boolean value) {
        isRetaining = value;
        // Propagate the retain notification down to any nested children.
        if (state != null) {
            for (int i = 0; i < state.size(); i++) {
                Object child = state.valueAt(i);
                if (child != null && child instanceof RetainState) {
                    ((RetainState) child).setIsRetaining(value);
                }
            }
        }
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
            if (item instanceof RetainState) {
                ((RetainState) item).setIsRetaining(isRetaining);
            }
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
            if (value instanceof RetainState) {
                ((RetainState) value).setIsRetaining(false);
            }
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
