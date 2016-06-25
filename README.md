# retain-state
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.tatarka.retainstate/retainstate/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/me.tatarka.retainstate/retainstate)

A dead simple way to retain some state thought configuration changes on Android

## What? Why?
Loading some data in a background thread and then showing it in your app is so common it should be trivial to do. Unfortunately Android does not make this easy. You have to deal with the fact that Activities can be destroyed out from under you at any time. This even happens even on a configuration change where you probably want to just continue whatever background work you are doing and show it in the newly-created Activity. Android does provided some components to allow to do this (Loaders, Fragments with `setRetainInstance(true)`, Services) but they are all overly complicated and have clunky and sometimes inflexible apis.

Luckily, there is a pair of methods that make retaining some state between configuration changes simple and easy and it's been there since Api 1: [onRetainNonConfigurationInstance](http://developer.android.com/reference/android/app/Activity.html#onRetainNonConfigurationInstance%28%29) and [getLastNonConfigutationInstance](http://developer.android.com/reference/android/app/Activity.html#getLastNonConfigurationInstance%28%29). This pair of methods allow you to preserve state across orientation changes! This library proviveds a super-simple (and by simple I mean < 200 loc) way to hook into this mechanism.

## Download

```groovy
compile 'me.tatarka.retainstate:retainstate:0.3'
```

## Usage
The first step is to hook up `RetainState` to your Activity. I'd advise to do this in your base Activity, but you can do this in whatever Activity makes sense for your application.

```java
public class BaseActivity extends Activity implements RetainState.Provider {
  private RetainState retainState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      retainState = new RetainState(getLastNonConfigurationInstance());
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
      return retainState.getState();
  }

  @Override
  public RetainState getRetainState() {
      if (retainState == null) {
          throw new IllegalStateException("RetainState has not yet been initialized");
      }
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

# Fragments

Optionally, you can extend support to fragments by nesting `RetainState` instances. Included is a library to easily obtain unique id's from fragments. Note that the fragment id's are negative, so you should use positive id's for other things.

```groovy
compile 'me.tatarka.retainstate:fragment:0.3'
```

Just make a fragment a provider, similarly to an activity.

```java
public class BaseFragment extends Fragment implements RetainState.Provider {
    private RetainState retainState;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        retainState = RetainState.from(getHost()).retain(RetainStateFragment.getId(this), RetainState.CREATE);
    }

    @Override
    public RetainState getRetainState() {
        if (retainState == null) {
            throw new IllegalStateException("RetainState has not yet been initialized");
        }
        return retainState;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isFinishing() || isRemoving()) {
            RetainState.from(getHost()).remove(RetainStateFragment.getId(this));
        }
    }
}
```

# Loader

This repo also includes a super-simple loader implementation built on top of retain-state. It lets you easily load something in the background and then get callbacks on the main thread that fire at the approriate times.

## Download

```groovy
compile 'me.tatarka.retainstate:loader:0.3'
// Contains an AsyncTaskLoader and CursorLoader to mirror the ones in the support lib.
compile 'me.tatarka.retainstate:loader-support:0.3'
// Takes an rxjava observable.
compile 'me.tatarka.retainstate:loader-rx:0.3'
```

## Usage

You obtain an instance of `LoaderManager` using retain-state to retain it, then you initialize one or more loaders with callbacks. Finally you use the methods `start()` or `restart()` on the loader to load the data and `cancel()` to cancel it. The callbacks will automatically re-deliver the correct results on a configuration change. Note: you do have to do a little cleanup when your Activity is destroyed to detach the callbacks.

```java
public class MainActivity extends BaseActivity {
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = RetainState.get(this).retain(R.id.my_loader_manager, LoaderManager.CREATE);

        final MyLoader loader = loaderManager.init(0, MyLoader.CREATE, new Loader.CallbacksAdapter<String>() {
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
        // Loader cleanup, detach() will remove the listeners, 
        // destroy() will additionally cancel and clean up the loaders
        if (isFinishing()) {
            loaderManager.destroy();
        } else {
            loaderManager.detach();
        }
    }
}
```

or in a fragment

```java
public class MyFragment extends BaseFragment {
    LoaderManager loaderManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loaderManager = RetainState.get(this).retain(R.id.my_loader_manager, LoaderManager.CREATE);
    
        final ModelLoader loader = loaderManager().init(0, ModelLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
              // Update your ui to show you are loading something
            }

            @Override
            public void onLoaderResult(String result) {
              // Update your ui with the result
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
    public void onStop() {
      super.onStop();
      if (loaderManager != null && isRemoving()) {
        loaderManager.detach();
      }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loaderManager != null) {
          if (getActivity().isFinishing() || isRemoving()) {
              loaderManager.destroy();
          } else {
              loaderManager.detach();
          }
        }
    }
}
```

To implement a loader, you subclass `Loader` and override `onStart()` and optionally `onCancel()` and `onDestroy()`.

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
    
    // Overriding this method is optional and allows you to clean up any resources.
    @Override
    protected void onDestroy() {
        
    }
}
```

## License

    Copyright 2015 Evan Tatarka
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
