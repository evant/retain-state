package me.tatarka.retainstate.fragment;

import android.app.Fragment;

import java.lang.reflect.Field;

/**
 * Helpers for using {@link me.tatarka.retainstate.RetainState} with fragments.
 */
public class RetainStateFragment {

    private static Field frameworkFragmentIndexField;
    private static Field supportFragmentIndexField;

    /**
     * Returns a unique id for the given fragment among it's peers in the fragment manager. These
     * id's are negative, allowing you to continue to use positive id's for other retained state.
     */
    public static int getId(Fragment fragment) {
        if (frameworkFragmentIndexField == null) {
            try {
                Field field = Fragment.class.getDeclaredField("mIndex");
                field.setAccessible(true);
                frameworkFragmentIndexField = field;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return getId(frameworkFragmentIndexField, fragment);
    }

    /**
     * Returns a unique id for the given fragment among it's peers in the fragment manager. These
     * id's are negative, allowing you to continue to use positive id's for other retained state.
     */
    public static int getId(android.support.v4.app.Fragment fragment) {
        if (supportFragmentIndexField == null) {
            try {
                Field field = android.support.v4.app.Fragment.class.getDeclaredField("mIndex");
                field.setAccessible(true);
                supportFragmentIndexField = field;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return getId(supportFragmentIndexField, fragment);
    }

    private static int getId(Field field, Object fragment) {
        try {
            int index = (int) field.get(fragment);
            if (index < 0) {
                throw new IllegalStateException("Fragment's index has not been initialized, you should wait until onActivityCreated() to call this method");
            }
            return -(index + 1);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
