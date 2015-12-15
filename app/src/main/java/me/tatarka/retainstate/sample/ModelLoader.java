package me.tatarka.retainstate.sample;

import android.os.Handler;
import android.os.Looper;

import me.tatarka.loader.Loader;
import me.tatarka.retainstate.RetainState;

public class ModelLoader extends Loader<String> {

    public static final RetainState.OnCreate<ModelLoader> CREATE = new RetainState.OnCreate<ModelLoader>() {
        @Override
        public ModelLoader onCreate() {
            return new ModelLoader();
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper());
    private Thread thread;

    @Override
    protected void onStart() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        deliverResult("Async Load finished");
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    protected void onStop() {
        thread.interrupt();
        thread = null;
    }
}
