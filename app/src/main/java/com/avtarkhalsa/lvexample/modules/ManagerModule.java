package com.avtarkhalsa.lvexample.modules;

import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.managers.QuestionManagerImpl;
import com.avtarkhalsa.lvexample.networking.APIInterface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
@Module
public class ManagerModule {
    @Provides
    @Singleton
    QuestionManager provideQuestionManager(APIInterface apiInterface){
        return new QuestionManagerImpl(apiInterface);
    }

}
