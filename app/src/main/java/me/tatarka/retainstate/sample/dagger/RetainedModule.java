package me.tatarka.retainstate.sample.dagger;

import dagger.Module;
import dagger.Provides;
import me.tatarka.loader.LoaderManager;

@Module
public class RetainedModule {
    @Provides
    @Retained
    public LoaderManager provideLoaderManager() {
        return new LoaderManager();
    }

    // Can provide anything else you want retained.
}
