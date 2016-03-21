package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.RetainState;

public abstract class BaseActivity extends AppCompatActivity implements RetainState.Provider {
    private RetainState retainState;
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retainState = new RetainState(getLastCustomNonConfigurationInstance());
        loaderManager = retainState.retain(R.id.my_loader, LoaderManager.CREATE);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return retainState.getState();
    }

    public LoaderManager loaderManager() {
        if (loaderManager == null) {
            throw new IllegalStateException("LoaderManager has not yet been initialized");
        }
        return loaderManager;
    }

    @Override
    public RetainState getRetainState() {
        if (retainState == null) {
            throw new IllegalStateException("RetainState has not yet been initialized");
        }
        return retainState;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            loaderManager.destroy();
        } else {
            loaderManager.detach();
        }
    }
}

