package com.avtarkhalsa.lvexample.networking;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class MockAPIInterface implements APIInterface {
    private List<NetworkQuestion> mockResponse;

    public MockAPIInterface(Context ctx){
        InputStream is = ctx.getResources().openRawResource(R.raw.mock_questions);
        Writer writer = new StringWriter();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                line = reader.readLine();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String mockJsonString = writer.toString();
        Gson gson = new Gson();
        NetworkQuestion[] arr = gson.fromJson(mockJsonString, NetworkQuestion[].class);
        mockResponse = Arrays.asList(arr);
    }
    @Override
    public Observable<List<NetworkQuestion>> getAllQuestions() {
        return Observable.defer(new Callable<ObservableSource<List<NetworkQuestion>>>() {
            @Override
            public ObservableSource<List<NetworkQuestion>> call() throws Exception {
                return Observable.just(mockResponse);
            }
        });
    }
}
