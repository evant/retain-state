# retain-state
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.tatarka.retainstate/retainstate/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/me.tatarka.retainstate/retainstate)
A dead simple way to retain some state thought configuration changes on Android

## What? Why?
Loading some data in a background thread and then showing it in your app is so common it should be trivial to do. Unfortunately Android does not make this easy. You have to deal with the fact that Activities can be destroyed out from under you at any time. This even happens even on a configuration change where you probably want to just continue whatever background work you are doing and show it in the newly-created Activity. Android does provided some components to allow to do this (Loaders, Fragments with `setRetainInstance(true)`, Services) but they are all overly complicated and have clunky and sometimes inflexible apis.

Luckily, there is a pair of methods that make retaining some state between configuration changes simple and easy and it's been there since Api 1: [onRetainNonConfigurationInstance](http://developer.android.com/reference/android/app/Activity.html#onRetainNonConfigurationInstance%28%29) and [getLastNonConfigutationInstance](http://developer.android.com/reference/android/app/Activity.html#getLastNonConfigurationInstance%28%29). This pair of methods allow you to preserve state across orientation changes! This library proviveds a super-simple (and by simple I mean < 200 loc) way to hook into this mechanism.

## Download

```groovy
compile 'me.tatarka.retainstate:retainstate:0.1'
```

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
    
    model = RetainState.get(this).retain(R.id.my_id, new RetainState.OnCreate<MyRetainedModel>() {
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

Additionally, these methods are used heavily by the support library to backport Loaders and Fragments. It would be infeasible for google to actually remove these methods any time in the near future, if at all because of this.

# Loader

This repo also includes a super-simple loader implementation built on top of retain-state. It lets you easily load something in the background and then get callbacks on the main thread that fire at the approriate times.

## Download

```groovy
compile 'me.tatarka.retainstate:loader:0.1'
```

## Usage

You obtain an instance of `LoaderManager` using retain-state to retain it, then you initilize one or more loaders with callbacks. Finnally you use the methods `start()`, `stop()`, and `restart()` on the loader to load the data. The callbacks will automatically re-deliver the correct results on a configuration change.

```java
public class MainActivity extends BaseActivity {
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = RetainState.get(this).retain(R.id.result_load_from_activity, LoaderManager.CREATE);

        final MyLoader loader = loaderManager.init(0, MyLoader.CREATE);
        loader.setCallbacks(new Loader.Callbacks<String>() {
            @Override
            public void onLoaderStart() {
              // Update your ui to show you are loading something
            }

            @Override
            public void onLoaderResult(String result) {
              // Update your ui with the result
            }

            @Override
            public void onLoaderComplete() {
              // Optionally do something when the loader has completed
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.restart();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Loader cleanup, detach() will remove the listeners, destroy() will additionally stop the loaders
        if (isFinishing()) {
            loaderManager.destroy();
        } else {
            loaderManager.detach();
        }
    }
}
```

To implement a loader, you subclass `Loader` and override `onStart()` and optionally `onCancel()`.

```java
public class MyLoader extends Loader<String> {
    // Convenience for loaderManager.init()
    public static final RetainState.OnCreate<MyLoader> CREATE = new RetainState.OnCreate<MyLoader>() {
        @Override
        public MyLoader onCreate() {
            return new MyLoader();
        }
    };
    
    @Override
    protected void onStart(Receiver receiver) {
        // Note loader doesn't handle threading, you have to do that yourself.
        api.doAsync(new ApiCallback() {
          public void onResult(String result) {
            // Make sure this happens on the main thread!
            receiver.deliverResult(result);
            receiver.complete();
          }
        });
    }

    // Overriding this method is optional, but if you can cancel your call when it's no longer needed, you should.
    @Override
    protected void onCancel() {
        api.cancel();
    }
}
```
