package me.tatarka.loader;

import android.util.SparseArray;

import me.tatarka.retainstate.RetainState;

/**
 * Manges a set of loaders in the same scope. You should obtain an instance with {@code
 * RetainState.get(context).retain(id, LoaderManager.CREATE) to retain it across configuration
 * changes.
 */
public class LoaderManager {

    public static final RetainState.OnCreate<LoaderManager> CREATE = new RetainState.OnCreate<LoaderManager>() {
        @Override
        public LoaderManager onCreate() {
            return new LoaderManager();
        }
    };

    private SparseArray<Loader<?>> loaders = new SparseArray<>(1);

    /**
     * Initializes a loader, creating it if it doesn't already exist.
     *
     * @param id        The id to init the loader with, this must be unique for this loader
     *                  manager.
     * @param create    Method for creating the loader if it does not already exist.
     * @param callbacks The loader callbacks. These may be called immediately if the loader is
     *                  already running.
     */
    public <T, L extends Loader<T>> L init(int id, RetainState.OnCreate<L> create, Loader.Callbacks<T> callbacks) {
        @SuppressWarnings("unchecked")
        L loader = (L) loaders.get(id);
        if (loader == null) {
            loader = create.onCreate();
            loaders.put(id, loader);
        }
        loader.setCallbacks(callbacks);
        return loader;
    }

    /**
     * Stops and removes the loader with the given id.
     */
    public void remove(int id) {
        Loader<?> loader = loaders.get(id);
        if (loader != null) {
            loader.setCallbacks(null);
            loader.stop();
            loaders.remove(id);
        }
    }

    /**
     * Detaches the callbacks from the loaders. You must call then when your context is being
     * destroyed to prevent leaks.
     */
    public void detach() {
        for (int i = 0, size = loaders.size(); i < size; i++) {
            Loader<?> loader = loaders.get(i);
            if (loader != null) {
                loader.setCallbacks(null);
            }
        }
    }

    /**
     * Detaches and stops all loaders. You may want to call this when you know you won't need any
     * anymore like when your activity is finishing.
     */
    public void destroy() {
        for (int i = 0, size = loaders.size(); i < size; i++) {
            Loader<?> loader = loaders.get(i);
            if (loader != null) {
                loader.setCallbacks(null);
                loader.stop();
            }
        }
    }
}
