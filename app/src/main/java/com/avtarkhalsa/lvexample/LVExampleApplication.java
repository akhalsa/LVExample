package com.avtarkhalsa.lvexample;

import android.app.Application;

import com.avtarkhalsa.lvexample.injector.APIComponent;
import com.avtarkhalsa.lvexample.injector.DaggerAPIComponent;
import com.avtarkhalsa.lvexample.modules.APIModule;
import com.avtarkhalsa.lvexample.modules.AppModule;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class LVExampleApplication extends Application {
    private APIComponent apiComponent;
    @Override
    public void onCreate() {
        super.onCreate();
        apiComponent = DaggerAPIComponent.builder()
                // list of modules that are part of this component need to be created here too
                .appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .aPIModule(new APIModule("some_url"))
                .build();
    }

    public APIComponent getAPIComponent() {
        return apiComponent;
    }
}
