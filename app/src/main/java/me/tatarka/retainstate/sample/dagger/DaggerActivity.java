package me.tatarka.retainstate.sample.dagger;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.sample.BaseActivity;
import me.tatarka.retainstate.sample.ModelLoader;
import me.tatarka.retainstate.sample.R;

public class DaggerActivity extends BaseActivity {
    @Inject
    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.retained(R.id.result_load_from_activity, this).inject(this);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.result_load_from_activity);
        final Button button = (Button) findViewById(R.id.button_load_from_activity);

        final ModelLoader loader = loaderManager.init(0, ModelLoader.CREATE);
        loader.setCallbacks(new Loader.Callbacks<String>() {
            @Override
            public void onLoaderStart() {
                textView.setText("Loading...");
            }

            @Override
            public void onLoaderResult(String result) {
                textView.setText(result);
            }

            @Override
            public void onLoaderComplete() {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.restart();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            DaggerLayout myLayout = (DaggerLayout) findViewById(R.id.my_layout);
            myLayout.finish();
            loaderManager.destroy();
        } else {
            loaderManager.detach();
        }
    }
}
