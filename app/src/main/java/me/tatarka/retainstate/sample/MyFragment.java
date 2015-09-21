package me.tatarka.retainstate.sample;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.retainstate.RetainState;

public class MyFragment extends Fragment {
    private RetainedModel model;

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
        model = RetainState.get(getActivity()).get(R.id.result_load_from_fragment, RetainedModel.onCreate());

        final TextView textView = (TextView) view.findViewById(R.id.result_load_from_fragment);
        Button button = (Button) view.findViewById(R.id.button_load_from_fragment);

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
    public void onDestroyView() {
        super.onDestroyView();
        // Don't want to leak fragment!
        model.setOnLoadFinishedListener(null);
    }
}
