package com.avtarkhalsa.lvexample.modules;

import com.avtarkhalsa.lvexample.expressions.SimpleBooleanEvaluator;
import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.managers.QuestionManagerImpl;
import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.networking.APIInterface;

import java.util.HashMap;

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
    SimpleBooleanEvaluator provideSimpleBooleanEvaluator(){
        return new SimpleBooleanEvaluator();
    }
    @Provides
    @Singleton
    QuestionManager provideQuestionManager(APIInterface apiInterface, SimpleBooleanEvaluator sbe){
        return new QuestionManagerImpl(apiInterface, sbe, new HashMap<Integer, Question>(), 0);
    }

}
