package me.tatarka.retainstate.sample;

import me.tatarka.loader.AsyncTaskLoader;
import me.tatarka.retainstate.RetainState;

public class ModelLoader extends AsyncTaskLoader<String> {

    public static final RetainState.OnCreate<ModelLoader> CREATE = new RetainState.OnCreate<ModelLoader>() {
        @Override
        public ModelLoader onCreate() {
            return new ModelLoader();
        }
    };

    @Override
    protected String doInBackground() {
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(200);
                if (!isRunning()) {
                    return null;
                }
            }
            return "Async Load finished";
        } catch (InterruptedException e) {
            return null;
        }
    }
}
