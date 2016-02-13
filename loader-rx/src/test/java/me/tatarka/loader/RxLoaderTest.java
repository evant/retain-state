package me.tatarka.loader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RxLoaderTest {

    TestScheduler scheduler;

    @Before
    public void setup() {
        scheduler = new TestScheduler();
    }

    @Test
    public void newlyCreatedIsNotSubscribed() {
        TestSubject<String> subject = TestSubject.create(scheduler);
        RxLoader<String> loader = new RxLoader<>(subject);

        assertFalse(subject.hasObservers());
    }

    @Test
    public void startedSubscribes() {
        TestSubject<String> subject = TestSubject.create(scheduler);
        RxLoader<String> loader = new RxLoader<>(subject);
        loader.start();

        assertTrue(subject.hasObservers());
    }

    @Test
    public void canceledUnsubscribes() {
        TestSubject<String> subject = TestSubject.create(scheduler);
        RxLoader<String> loader = new RxLoader<>(subject);
        loader.start();
        loader.cancel();

        assertFalse(subject.hasObservers());
    }

    @Test
    public void onNextCallsCallbacks() {
        TestSubject<String> subject = TestSubject.create(scheduler);
        Loader.Callbacks<Result<String>> callbacks = mock(Loader.Callbacks.class);
        RxLoader<String> loader = new RxLoader<>(subject);
        loader.setCallbacks(callbacks);
        loader.start();
        subject.onNext("test");
        
        scheduler.triggerActions();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(Result.success("test"));
    }

    @Test
    public void onErrorCallsCallback() {
        TestSubject<String> subject = TestSubject.create(scheduler);
        Loader.Callbacks<Result<String>> callbacks = mock(Loader.Callbacks.class);
        Exception error = new Exception();
        RxLoader<String> loader = new RxLoader<>(subject);
        loader.setCallbacks(callbacks);
        loader.start();
        subject.onError(error);

        scheduler.triggerActions();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(Result.<String>error(error));
        verify(callbacks).onLoaderComplete();
    }

    @Test
    public void onCompleteCallsCallback() {
        TestSubject<String> subject = TestSubject.create(scheduler);
        Loader.Callbacks<Result<String>> callbacks = mock(Loader.Callbacks.class);
        RxLoader<String> loader = new RxLoader<>(subject);
        loader.setCallbacks(callbacks);
        loader.start();
        subject.onCompleted();

        scheduler.triggerActions();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderComplete();
    }
}
