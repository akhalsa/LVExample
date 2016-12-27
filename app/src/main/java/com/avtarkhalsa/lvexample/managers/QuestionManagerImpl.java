package com.avtarkhalsa.lvexample.managers;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class QuestionManagerImpl implements QuestionManager {
    private Observable<Question> networkStream;

    private int currentQuestion;
    private int questionsLength;
    public QuestionManagerImpl(APIInterface api){
        networkStream = api.getAllQuestions()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<List<NetworkQuestion>, Observable<NetworkQuestion>>() {
                    @Override
                    public Observable<NetworkQuestion> apply(List<NetworkQuestion> networkQuestions) throws Exception {
                        questionsLength = networkQuestions.size();
                        return Observable.fromArray(networkQuestions.toArray(new NetworkQuestion[questionsLength]));
                    }
                })
                .map(new Function<NetworkQuestion, Question>() {
                    @Override
                    public Question apply(NetworkQuestion networkQuestion) throws Exception {
                        return new Question(networkQuestion);
                    }
                });
        currentQuestion = 0;
        questionsLength = 0;
    }

    public Maybe<Question> loadNextQuestion(){
        int i = currentQuestion;
        currentQuestion++;
        return networkStream.elementAt((long)i);
    }
}

