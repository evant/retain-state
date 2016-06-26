package me.tatarka.retainstate.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import me.tatarka.retainstate.RetainState;

/**
 * Helpers for using {@link me.tatarka.retainstate.RetainState} with fragments.
 */
public class RetainStateFragment {

    public static final int LOADER_ID = -1;

    /**
     * Returns a {@link RetainState} scoped to the given fragment. The implementation uses a system
     * loader. If you want to use your own loaders, you should ensure they don't collide with the id
     * {@link #LOADER_ID}. However, you'd probably want to use {@code me.tatarka.loader.Loader}
     * instead since it has a nicer interface.
     */
    public static RetainState from(Fragment fragment) {
        RetainStateFragmentCallbacks callbacks = new RetainStateFragmentCallbacks(fragment.getContext());
        fragment.getLoaderManager().initLoader(LOADER_ID, null, callbacks);
        return ((RetainStateFragmentLoader) fragment.getLoaderManager().<RetainState>getLoader(LOADER_ID)).retainState;
    }

    static class RetainStateFragmentLoader extends Loader<RetainState> {
        final RetainState retainState;

        public RetainStateFragmentLoader(Context context) {
            super(context);
            retainState = new RetainState(null);
            retainState.onRetain();
        }

        @Override
        protected void onReset() {
            retainState.destroy();
        }
    }

    static class RetainStateFragmentCallbacks implements LoaderManager.LoaderCallbacks<RetainState> {
        final Context context;

        RetainStateFragmentCallbacks(Context context) {
            this.context = context;
        }

        @Override
        public Loader<RetainState> onCreateLoader(int id, Bundle args) {
            return new RetainStateFragmentLoader(context);
        }

        @Override
        public void onLoadFinished(Loader<RetainState> loader, RetainState data) {
        }

        @Override
        public void onLoaderReset(Loader<RetainState> loader) {

        }
    }
}
