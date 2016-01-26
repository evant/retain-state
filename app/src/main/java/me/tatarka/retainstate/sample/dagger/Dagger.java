package me.tatarka.retainstate.sample.dagger;

import android.content.Context;

import me.tatarka.retainstate.RetainState;

public class Dagger {
    public static RetainedComponent retained(int id, Context context) {
        return RetainState.get(context).retain(id, new RetainState.OnCreate<RetainedComponent>() {
            @Override
            public RetainedComponent onCreate() {
                return DaggerRetainedComponent.builder()
                        .retainedModule(new RetainedModule())
                        .build();
            }
        });
    }
}
