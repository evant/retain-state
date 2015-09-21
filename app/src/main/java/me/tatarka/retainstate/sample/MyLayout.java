package me.tatarka.retainstate.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.tatarka.retainstate.RetainState;

public class MyLayout extends LinearLayout {
    private RetainedModel model;
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
        model = RetainState.get(getContext()).state(R.id.result_load_from_custom_view, RetainedModel.onCreate());
        
        model.setOnLoadFinishedListener(new RetainedModel.OnLoadFinishedListener() {
            @Override
            public void onLoadFinished(String result) {
                textView.setText(result);
            }
        });
        
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(null);
                model.load();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Don't want to leak the view!
        model.setOnLoadFinishedListener(null);
    }
}
