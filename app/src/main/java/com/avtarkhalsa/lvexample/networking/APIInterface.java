package com.avtarkhalsa.lvexample.networking;


import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public interface APIInterface {
    @GET("/questions")
    Observable<List<NetworkQuestion>> getAllQuestions();
}
