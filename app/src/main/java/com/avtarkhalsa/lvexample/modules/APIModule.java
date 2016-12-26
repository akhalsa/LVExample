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
     * be to uncomment the provideRealInterface method and to comment out the provideFakeInterface method
     *
     * There are better ways to do this if you only want to use the mock data in test code, but since we want to be
     * able to run it as though the mocked responses are coming from a server, this seems simplest
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

