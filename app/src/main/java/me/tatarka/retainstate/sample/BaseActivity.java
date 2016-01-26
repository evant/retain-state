package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.tatarka.retainstate.RetainState;

public abstract class BaseActivity extends AppCompatActivity implements RetainState.Provider {
    private RetainState retainState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        retainState = new RetainState(getLastCustomNonConfigurationInstance());
        super.onCreate(savedInstanceState);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return retainState.getState();
    }

    @Override
    public RetainState getRetainState() {
        return retainState;
    }
}

