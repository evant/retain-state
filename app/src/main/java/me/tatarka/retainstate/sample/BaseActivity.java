package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.tatarka.retainstate.RetainState;

public class BaseActivity extends AppCompatActivity implements RetainState.Provider {
    private RetainState retainState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retainState = new RetainState(this);
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
