package me.tatarka.retainstate.sample.retainedfragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import me.tatarka.retainstate.RetainState;

/**
 * Example using a retained fragment if you <em>really</em> don't want to use those deprecated
 * methods.
 */
public class BaseActivity extends AppCompatActivity implements RetainState.Provider {
    private static final String RETAIN_TAG = "me.tatarka.retainstate.RETAIN_TAG";
    private RetainedFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragment = new RetainedFragment();
            fm.beginTransaction()
                    .add(fragment, RETAIN_TAG)
                    .commit();
            fm.executePendingTransactions();
        } else {
            fragment = (RetainedFragment) fm.findFragmentByTag(RETAIN_TAG);
        }
    }

    @Override
    public RetainState getRetainState() {
        return fragment.retainState;
    }

    public static final class RetainedFragment extends Fragment {
        private final RetainState retainState = new RetainState(null);

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
