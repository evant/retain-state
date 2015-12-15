package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.RetainState;

public class MainActivity extends BaseActivity {
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loaderManager = RetainState.get(this).retain(R.id.result_load_from_activity, LoaderManager.CREATE);

        final TextView textView = (TextView) findViewById(R.id.result_load_from_activity);
        final Button button = (Button) findViewById(R.id.button_load_from_activity);

        final ModelLoader loader = loaderManager.init(0, ModelLoader.CREATE, new Loader.Callbacks<String>() {
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
            MyLayout myLayout = (MyLayout) findViewById(R.id.my_layout);
            myLayout.finish();
            loaderManager.destroy();
        } else {
            loaderManager.detach();
        }
    }
}
