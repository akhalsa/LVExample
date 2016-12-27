package com.avtarkhalsa.lvexample.injector;

import com.avtarkhalsa.lvexample.activities.MainActivity;
import com.avtarkhalsa.lvexample.modules.APIModule;
import com.avtarkhalsa.lvexample.modules.AppModule;
import com.avtarkhalsa.lvexample.modules.ManagerModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by avtarkhalsa on 12/25/16.
 */

@Singleton
@Component(modules={AppModule.class, APIModule.class, ManagerModule.class})
public interface AppComponent {
    void inject(MainActivity activity);
}
