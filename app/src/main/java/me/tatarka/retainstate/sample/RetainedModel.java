package me.tatarka.retainstate.sample;

import android.os.Handler;
import android.os.Looper;

import me.tatarka.retainstate.RetainState;

/**
 * Model that does stuff in the background. Doing it with a dedicated thread and handler, but you
 * can do this however you want.
 */
public class RetainedModel {

    public static RetainState.OnCreate<RetainedModel> onCreate() {
        return new RetainState.OnCreate<RetainedModel>() {
            @Override
            public RetainedModel onCreate() {
                return new RetainedModel();
            }
        };
    }

    private String result;
    private OnLoadFinishedListener onLoadFinishedListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public void load() {
        result = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                result = "Async Load Finished";
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoadFinishedListener != null) {
                            onLoadFinishedListener.onLoadFinished(result);
                        }
                    }
                });
            }
        }).start();
    }

    public void setOnLoadFinishedListener(OnLoadFinishedListener listener) {
        onLoadFinishedListener = listener;
        if (listener != null && result != null) {
            listener.onLoadFinished(result);
        }
    }

    public interface OnLoadFinishedListener {
        void onLoadFinished(String result);
    }
}
