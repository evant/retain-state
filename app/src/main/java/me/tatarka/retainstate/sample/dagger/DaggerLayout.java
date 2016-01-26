package me.tatarka.retainstate.sample.dagger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.sample.ModelLoader;
import me.tatarka.retainstate.sample.R;
import me.tatarka.retainstate.sample.dagger.Dagger;

public class DaggerLayout extends LinearLayout {
    @Inject
    LoaderManager loaderManager;
    private TextView textView;
    private Button button;

    public DaggerLayout(Context context) {
        this(context, null);
    }

    public DaggerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DaggerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
        Dagger.retained(R.id.result_load_from_custom_view, getContext()).inject(this);

        final ModelLoader loader = loaderManager.init(0, ModelLoader.CREATE);
        loader.setCallbacks(new Loader.CallbacksAdapter<String>() {
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
