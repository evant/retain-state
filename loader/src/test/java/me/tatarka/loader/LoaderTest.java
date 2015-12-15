package me.tatarka.loader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(JUnit4.class)
public class LoaderTest {

    @Spy
    Loader<String> loader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void newlyCreatedLoaderIsNotRunning() {
        assertFalse(loader.isRunning());
    }

    @Test
    public void newlyCreatedLoaderHasNoResult() {
        assertFalse(loader.hasResult());
    }

    @Test
    public void startedLoaderIsRunning() {
        loader.start();

        assertTrue(loader.isRunning());
    }

    @Test
    public void startedLoaderHasNoResult() {
        loader.start();

        assertFalse(loader.hasResult());
    }

    @Test
    public void startingLoaderCallsOnStart() {
        loader.start();

        verify(loader).onStart();
    }

    @Test
    public void deliverResultIsRunning() {
        loader.start();
        loader.deliverResult("test");

        assertTrue(loader.isRunning());
    }

    @Test
    public void deliverResultsHasResult() {
        loader.start();
        loader.deliverResult("test");

        assertTrue(loader.hasResult());
    }

    @Test
    public void completeIsNotRunning() {
        loader.start();
        loader.complete();

        assertFalse(loader.isRunning());
    }

    @Test
    public void completeHasNoResult() {
        loader.start();
        loader.complete();

        assertFalse(loader.hasResult());
    }

    @Test
    public void deliverResultCompleteIsNotRunning() {
        loader.start();
        loader.deliverResult("test");
        loader.complete();

        assertFalse(loader.isRunning());
    }

    @Test
    public void deliverResultCompleteHasResult() {
        loader.start();
        loader.deliverResult("test");
        loader.complete();

        assertTrue(loader.hasResult());
    }

    @Test
    public void stopWithoutStartDoesNothing() {
        loader.stop();

        verify(loader, never()).onStop();
    }

    @Test
    public void startStopCallsOnStop() {
        loader.start();
        loader.stop();
        verify(loader).onStop();
    }

    @Test
    public void startStopIsNotRunning() {
        loader.start();
        loader.stop();

        assertFalse(loader.isRunning());
    }

    @Test
    public void startStopHasNoResult() {
        loader.start();
        loader.stop();

        assertFalse(loader.hasResult());
    }

    @Test
    public void startCompleteStopDoesNotCallOnStop() {
        loader.start();
        loader.complete();
        loader.stop();

        verify(loader, never()).onStop();
    }

    @Test
    public void newlyCreatedDoesNotCallCallbacks() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);

        verifyZeroInteractions(callbacks);
    }

    @Test
    public void startCallsCallbacksOnLoadStart() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void runningCallsCallbacksOnLoadStart() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.start();
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void deliverResultCallsCallbacksOnLoaderResult() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.deliverResult("test");

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(eq("test"));
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void deliveredResultCallsCallbacksOnLoaderResult() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.start();
        loader.deliverResult("test");
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderResult(eq("test"));
        verifyNoMoreInteractions(callbacks);
    }
    
    @Test
    public void completeCallsCallbacksLoaderComplete() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.complete();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderComplete();
        verifyNoMoreInteractions(callbacks);
    }
    
    @Test
    public void completedCallsCallbacksLoaderComplete() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.start();
        loader.complete();
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderComplete();
        verifyNoMoreInteractions(callbacks);
    }
}
