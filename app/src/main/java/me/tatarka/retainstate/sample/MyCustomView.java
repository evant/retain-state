package me.tatarka.retainstate.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.RetainState;

public class MyCustomView extends LinearLayout {
    private RetainState retainState;
    private LoaderManager loaderManager;

    public MyCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        retainState = RetainState.from(context).retain(R.id.my_custom_view, RetainState.CREATE);
        loaderManager = retainState.retain(R.id.my_loader, LoaderManager.CREATE);
        inflate(context, R.layout.include_my_view, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final TextView textView = (TextView) findViewById(R.id.result_load_from_custom_view);
        Button button = (Button) findViewById(R.id.button_load_from_custom_view);

        final ModelLoader loader = loaderManager.init(0, ModelLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                textView.setText("Loading...");
            }

            @Override
            public void onLoaderResult(String result) {
                textView.setText(result);
            }
        });

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.restart();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loaderManager.onDestroy(retainState);
        if (!retainState.isRetaining()) {
            RetainState.from(getContext()).remove(R.id.my_custom_view);
        }
    }
}
