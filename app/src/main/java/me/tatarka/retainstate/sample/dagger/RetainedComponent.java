package me.tatarka.retainstate.sample.dagger;

import dagger.Component;

@Retained
@Component(modules = RetainedModule.class)
public interface RetainedComponent {

    void inject(DaggerActivity activity);

    void inject(DaggerFragment fragment);

    void inject(DaggerLayout layout);
}
