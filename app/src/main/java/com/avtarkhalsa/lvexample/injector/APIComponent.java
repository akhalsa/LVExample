package com.avtarkhalsa.lvexample.injector;

import com.avtarkhalsa.lvexample.activities.MainActivity;
import com.avtarkhalsa.lvexample.modules.APIModule;
import com.avtarkhalsa.lvexample.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by avtarkhalsa on 12/25/16.
 */

@Singleton
@Component(modules={AppModule.class, APIModule.class})
public interface APIComponent {
    void inject(MainActivity activity);
}
