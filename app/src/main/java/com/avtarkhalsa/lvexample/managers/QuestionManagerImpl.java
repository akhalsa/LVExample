package com.avtarkhalsa.lvexample.managers;

import android.os.Looper;
import android.util.Log;

import com.avtarkhalsa.lvexample.models.BaseQuestion;
import com.avtarkhalsa.lvexample.models.QuestionType;
import com.avtarkhalsa.lvexample.models.TextualQuestion;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class QuestionManagerImpl implements QuestionManager {
    private APIInterface apiInterface;
    private Observable<BaseQuestion> networkStream;

    private int currentQuestion;
    private int questionsLength;
    public QuestionManagerImpl(APIInterface api){
        apiInterface = api;
        networkStream = api.getAllQuestions()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<List<NetworkQuestion>, Observable<NetworkQuestion>>() {
                    @Override
                    public Observable<NetworkQuestion> apply(List<NetworkQuestion> networkQuestions) throws Exception {
                        questionsLength = networkQuestions.size();
                        return Observable.fromArray(networkQuestions.toArray(new NetworkQuestion[questionsLength]));
                    }
                })
                .map(new Function<NetworkQuestion, BaseQuestion>() {
                    @Override
                    public BaseQuestion apply(NetworkQuestion networkQuestion) throws Exception {
                        return instantiateQuestion(networkQuestion);
                    }
                });
        currentQuestion = 0;
        questionsLength = 0;
    }

    public Maybe<BaseQuestion> loadNextQuestion(){
        int i = currentQuestion;
        currentQuestion++;
        return networkStream.elementAt((long)i);
    }

    private BaseQuestion instantiateQuestion(NetworkQuestion nq){
        QuestionType qt = QuestionType.fromString(nq.getQuestion_type());
        switch(qt){
            case Textual:
                return new TextualQuestion(nq.getQuestion_label());
            case Numerical:
                return new TextualQuestion(nq.getQuestion_label());
            default:
                return null;
        }
    }
}

