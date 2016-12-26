package com.avtarkhalsa.lvexample.modules;

import android.app.Application;

import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networking.MockAPIInterface;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
@Module
public class APIModule {
    /**
     * Most of these are not necessary until you actually swap out the mock response with a real response
     * That said, its good to keep infrastructure like this in place. All that would be needed to switch to a real server would
     * be to uncomment the provideRetrofit method
     */
    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        return new OkHttpClient.Builder().cache(cache).build();
    }

    //uncomment this, and comment out the provideFakeInterface method to switch to an actual server
    //you would also need to supply a baseUrl

    /*@Provides
    @Singleton
    APIInterface provideRealInterface(Gson gson, OkHttpClient okHttpClient, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
        return retrofit.create(APIInterface.class);
    }*/

    @Provides
    @Singleton
    APIInterface provideFakeInterface(Application application) {
        return new MockAPIInterface(application);
    }

}

