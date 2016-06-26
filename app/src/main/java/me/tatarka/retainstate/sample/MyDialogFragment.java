package me.tatarka.retainstate.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.tatarka.loader.Loader;
import me.tatarka.loader.LoaderManager;
import me.tatarka.retainstate.RetainState;
import me.tatarka.retainstate.fragment.RetainStateFragment;

public class MyDialogFragment extends DialogFragment {
    private RetainState retainState;
    private LoaderManager loaderManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        retainState = RetainStateFragment.from(this);
        loaderManager = retainState.retain(R.id.my_loader, me.tatarka.loader.LoaderManager.CREATE);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), getTheme());
        dialog.setContentView(R.layout.fragment_my);

        final TextView textView = (TextView) dialog.findViewById(R.id.result_load_from_fragment);
        Button button = (Button) dialog.findViewById(R.id.button_load_from_fragment);

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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.restart();
            }
        });

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loaderManager.onDestroy(retainState);
    }
}
