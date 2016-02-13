package me.tatarka.loader;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkEnvironment;

public class MyRobolectricTestRunner extends RobolectricGradleTestRunner {
    public MyRobolectricTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
        super.configureShadows(sdkEnvironment, config);
    }
}
