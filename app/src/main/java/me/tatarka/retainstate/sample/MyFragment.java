package me.tatarka.retainstate.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.loader.Loader;

public class MyFragment extends BaseFragment {

    private TextView textView;
    private Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        textView = (TextView) view.findViewById(R.id.result_load_from_fragment);
        button = (Button) view.findViewById(R.id.button_load_from_fragment);

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
    }
}
