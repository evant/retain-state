# retain-state
A dead simple way to retain some state thought configuration changes on Android

## What? Why?
Loading some data in a background thread and then showing it in your app is so common it should be trival to do. Unfortuantly Android does not make this easy. You have to deal with the fact that Activities can be destroyed out from under you at any time. This even happens even on a configuration change where you probably want to just continue whatever background work you are doing and show it in the newly-created Activity. Android does provided some components to allow to do this (Loaders, Fragments with `setRetainInstance(true)`, Services) but they are all overly complicated and have clunky and sometimes inflexible apis.

Luckily, there is a pair of methods that make retaining some state between configuration changes simple and easy and it's been there since Api 1: [onRetainNonConfigurationInstance](http://developer.android.com/reference/android/app/Activity.html#onRetainNonConfigurationInstance%28%29) and [getLastNonConfigutationInstance](http://developer.android.com/reference/android/app/Activity.html#getLastNonConfigurationInstance%28%29). This pair of methods allow you to preserve state across orientation changes! This library proviveds a super-simple (and by simple I mean < 200 loc) way to hook into this mechanizim.

## Download
This library is currently not on maven central. However, it's so small that it's fine for you to just copy the one class into your application. https://raw.githubusercontent.com/evant/retain-state/master/retainstate/src/main/java/me/tatarka/retainstate/RetainState.java

## Usage
The first step is to hook up `RetainState` to your Activity. I'd advise to do this in your base Activity, but you can do this in whatever Activity makes sense for your application.

```java
public class BaseActivity extends Activity implements RetainState.Provider {
  private RetainState retainState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    retainState = new RetainState(getLastNonConfigurationInstance());
    super.onCreate(savedInstanceState);
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
      return retainState.getState();
  }

  @Override
  public RetainState getRetainState() {
      return retainState;
  }
}
```

Note: If you are using `FragmentActivity` or `AppCompatActivity` you have to use `getLastCustomNonConfigurationInstance()` and override `onRetainCustomNonConfigurationInstance()` instead.

Now you just have to use `RetainState` to obtain the instance you want to retain.

```java
public class MainActivity extends BaseActivity {
  private MyRetainedModel model;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    model = RetainState.get(this).state(R.id.my_id, new RetainState.OnCreate<MyRetainedModel>() {
      @Override
      public MyRetainedModel onCreate() {
        return new MyRetainedModel();
      }
    });

    // Do stuff with model
    model.setListener(...);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Make sure you remove any references that can cause your Activity to leak!
    model.setListener(null);
  }
}
```

`RetainState.get()` can be used in any place where the Activity's context is available, like in a Fragment or a custom View.

Note: Your id's *must* be unique for the given Activity. You can achieve this by using view id's, you own generated id's, or by hand crafting them yourself.

## Aren't these methods deprecated?
The documentation states:
>  This method was deprecated in API level 13.
Use the new Fragment API setRetainInstance(boolean) instead; this is also available on older platforms through the Android compatibility package.

This is silly, there are many instances when using a Fragment doesn't make sense, and Fragments retained this way can't even be nested! See https://code.google.com/p/android/issues/detail?id=151346 for more reasons why these methods should not be deprecated.

Additinaly, these methods are used heavily by the support library to backport Loaders and Fragments. It would be infeasible for google to actually remove these methods any time in the near future, if at all because of this.
