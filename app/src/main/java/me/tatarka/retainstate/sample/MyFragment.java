package me.tatarka.retainstate.sample;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.RetainState;

public class MyFragment extends Fragment {
    private LoaderManager loaderManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        loaderManager = RetainState.get(getActivity()).retain(R.id.result_load_from_fragment, LoaderManager.CREATE);

        final TextView textView = (TextView) view.findViewById(R.id.result_load_from_fragment);
        Button button = (Button) view.findViewById(R.id.button_load_from_fragment);

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
    public void onDestroyView() {
        super.onDestroyView();
        if (isRemoving() || getActivity().isFinishing()) {
            loaderManager.destroy();
            if (isRemoving()) {
                RetainState.get(getActivity()).remove(R.id.result_load_from_fragment);
            }
        } else {
            loaderManager.detach();
        }
    }
}
