package me.tatarka.retainstate.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.RetainState;

public class MyLayout extends LinearLayout {
    private LoaderManager loaderManager;
    private TextView textView;
    private Button button;

    public MyLayout(Context context) {
        this(context, null);
    }

    public MyLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        inflate(context, R.layout.include_my_view, this);
        textView = (TextView) findViewById(R.id.result_load_from_custom_view);
        button = (Button) findViewById(R.id.button_load_from_custom_view);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        loaderManager = RetainState.get(getContext()).retain(R.id.result_load_from_custom_view, LoaderManager.CREATE);
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
        loaderManager.detach();
    }

    public void finish() {
        loaderManager.destroy();
    }
}
