package com.avtarkhalsa.lvexample.managers;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class QuestionManagerImpl implements QuestionManager {
    private ReplaySubject<Question> networkStream;

    private HashMap<Integer, Question> completedQuestionsLookup;
    private int questionsLength;
    public QuestionManagerImpl(APIInterface api){
        Observable<Question> networkObservable = api.getAllQuestions()
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
        networkStream = ReplaySubject.create();
        networkObservable.subscribe(networkStream);
        questionsLength = 0;
        completedQuestionsLookup = new HashMap<>();
    }

    public Maybe<Question> loadFirstQuestion(){
        return networkStream.elementAt((long)0);
    }

    @Override
    public Maybe<Question> setStringResponseForQuestion(String response, Question question) {
        //any syncing with the api can be done from here if necessary
        question.setResponse(response);
        return loadNextQuestion(question);
    }

    @Override
    public Maybe<Question> setNumberResponseForQuestion(Double response, Question question) {
        //any syncing with the api can be done from here if necessary
        if(response == null){
            question.setResponse(null);
        }else{
            question.setResponse(Double.valueOf(response).toString());
        }
        return loadNextQuestion(question);
    }

    @Override
    public Maybe<Question> setChoicesResponseForQuestion(Integer[] choice_indicies, Question question) {
        //any syncing with the api can be done from here if necessary
        StringBuilder sb = new StringBuilder();
        for (int i : choice_indicies){
            sb.append(question.getChoices().get(i));
        }
        question.setResponse(sb.toString());
        return loadNextQuestion(question);
    }

    @Override
    public Question loadCompletedQuestionWithId(int question_id){
        return null;
    }

    private Maybe<Question> loadNextQuestion(Question q){
        completedQuestionsLookup.put(q.getId(), q);
        final int new_id = q.getId()+1;
        return networkStream
                .filter(
                    new Predicate<Question>() {
                        @Override
                        public boolean test(Question question) throws Exception {
                            return question.getId() == new_id;
                        }
                    }
                )
                .firstElement();
    }
}

