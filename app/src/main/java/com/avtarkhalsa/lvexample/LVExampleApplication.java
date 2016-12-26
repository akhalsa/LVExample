package com.avtarkhalsa.lvexample;

import android.app.Application;

import com.avtarkhalsa.lvexample.injector.AppComponent;
import com.avtarkhalsa.lvexample.injector.DaggerAppComponent;
import com.avtarkhalsa.lvexample.modules.APIModule;
import com.avtarkhalsa.lvexample.modules.AppModule;
import com.avtarkhalsa.lvexample.modules.ManagerModule;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class LVExampleApplication extends Application {
    private AppComponent appComponent;
    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                // list of modules that are part of this component need to be created here too
                .appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .managerModule(new ManagerModule())
                .aPIModule(new APIModule("some_url"))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
