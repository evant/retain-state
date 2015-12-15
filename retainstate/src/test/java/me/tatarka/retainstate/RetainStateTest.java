package me.tatarka.retainstate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Need Robolectric for {@link android.util.SparseArray} to work.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class RetainStateTest {
    @Test
    public void retain_creates_object() throws Exception {
        RetainState retainState = new RetainState(null);
        String result = retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test";
            }
        });

        assertEquals("test", result);
    }

    @Test
    public void retain_returns_existing_retained_object() throws Exception {
        RetainState retainState = new RetainState(null);
        retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test";
            }
        });
        String result = retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                throw new AssertionError("Expected OnCreate to not be called when result is already retained");
            }
        });

        assertEquals("test", result);
    }

    @Test
    public void retain_supports_multiple_unique_ids() throws Exception {
        RetainState retainState = new RetainState(null);
        String result1 = retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test1";
            }
        });
        String result2 = retainState.retain(1, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test2";
            }
        });

        assertEquals("test1", result1);
        assertEquals("test2", result2);
    }

    @Test
    public void get_returns_null_for_nonexistent_object() throws Exception {
        RetainState retainState = new RetainState(null);
        Object result = retainState.get(0);

        assertNull(result);
    }

    @Test
    public void get_returns_retained_object() throws Exception {
        RetainState retainState = new RetainState(null);
        retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test";
            }
        });
        String result = retainState.get(0);

        assertEquals("test", result);
    }

    @Test
    public void remove_returns_null_for_nonexistent_object() throws Exception {
        RetainState retainState = new RetainState(null);
        Object result = retainState.remove(0);

        assertNull(result);
    }

    @Test
    public void remove_removes_and_returns_retained_object() throws Exception {
        RetainState retainState = new RetainState(null);
        retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test";
            }
        });
        String result1 = retainState.remove(0);
        Object result2 = retainState.get(0);

        assertEquals("test", result1);
        assertNull(result2);
    }

    @Test
    public void can_save_and_restore_state() throws Exception {
        RetainState retainState = new RetainState(null);
        retainState.retain(0, new RetainState.OnCreate<String>() {
            @Override
            public String onCreate() {
                return "test";
            }
        });
        Object state = retainState.getState();
        retainState = new RetainState(state);
        String result = retainState.get(0);

        assertEquals("test", result);
    }
}