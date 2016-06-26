package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.loader.Loader;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.result_load_from_activity);
        final Button button = (Button) findViewById(R.id.button_load_from_activity);
        final Button dialgoFragmentButotn = (Button) findViewById(R.id.dialog_fragment);

        final ModelLoader loader = loaderManager().init(0, ModelLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                textView.setText("Loading...");
            }

            @Override
            public void onLoaderResult(String result) {
                textView.setText(result);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.restart();
            }
        });

        dialgoFragmentButotn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .add(new MyDialogFragment(), "TAG")
                        .commitNow();
            }
        });
    }
}
