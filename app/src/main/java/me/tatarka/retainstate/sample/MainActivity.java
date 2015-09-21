package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.retainstate.RetainState;

public class MainActivity extends BaseActivity {

    private RetainedModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.result_load_from_activity);
        final Button button = (Button) findViewById(R.id.button_load_from_activity);

        model = getRetainState().get(R.id.result_load_from_activity, RetainedModel.onCreate());

        model.setOnLoadFinishedListener(new RetainedModel.OnLoadFinishedListener() {
            @Override
            public void onLoadFinished(String result) {
                textView.setText(result);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(null);
                model.load();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't want to leak Activity!
        model.setOnLoadFinishedListener(null); 
    }
}
