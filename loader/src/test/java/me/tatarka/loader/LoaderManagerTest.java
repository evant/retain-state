package me.tatarka.loader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LoaderManagerTest {

    LoaderManager loaderManager;

    @Before
    public void setup() {
        loaderManager = new LoaderManager();
    }

    @Test
    public void init() {
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create());

        assertNotNull(loader);
    }

    @Test
    public void initTwice() {
        TestLoader<String> loader1 = loaderManager.init(0, TestLoader.<String>create());
        TestLoader<String> loader2 = loaderManager.init(0, TestLoader.<String>create());

        assertTrue(loader1 == loader2);
    }

    @Test
    public void initDifferentIds() {
        TestLoader<String> loader1 = loaderManager.init(0, TestLoader.<String>create());
        TestLoader<String> loader2 = loaderManager.init(1, TestLoader.<String>create());

        assertFalse(loader1 == loader2);
    }

    @Test
    public void detach() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create());
        loader.setCallbacks(callbacks);
        loader.start();
        loaderManager.detach();
        loader.deliverResult("test");

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void destroy() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create());
        loader.setCallbacks(callbacks);
        loader.start();
        loaderManager.destroy();
        loader.deliverResult("test");

        assertTrue(loader.isCanceled());
        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void detachAndReattach() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create());
        loader.setCallbacks(callbacks);
        loader.start();
        loaderManager.detach();
        loader.deliverResult("test");

        loader = loaderManager.init(0, TestLoader.<String>create());
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(eq("test"));
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void attachTwice() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create());
        loader.setCallbacks(callbacks);

        try {
            loaderManager.init(0, TestLoader.<String>create());
            fail();
        } catch (IllegalStateException e) {
            // pass
        }
    }
}
