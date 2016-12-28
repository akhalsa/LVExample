package com.avtarkhalsa.lvexample.managers;

import android.util.Log;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkCondition;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.networkmodels.NetworkTakeAway;
import com.google.gson.Gson;

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

    private ReplaySubject<NetworkQuestion> networkStream;

    private HashMap<Integer, Question> completedQuestionsLookup;
    public QuestionManagerImpl(APIInterface api){
        Observable<NetworkQuestion> networkObservable = api.getAllQuestions()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<List<NetworkQuestion>, Observable<NetworkQuestion>>() {
                    @Override
                    public Observable<NetworkQuestion> apply(List<NetworkQuestion> networkQuestions) throws Exception {
                        return Observable.fromArray(networkQuestions.toArray(new NetworkQuestion[networkQuestions.size()]));
                    }
                });
        networkStream = ReplaySubject.create();
        networkObservable.subscribe(networkStream);
        completedQuestionsLookup = new HashMap<>();
    }

    public Maybe<Question> loadFirstQuestion(){
        return networkStream
                .elementAt((long)0)
                .map(new Function<NetworkQuestion, Question>() {
                    @Override
                    public Question apply(NetworkQuestion networkQuestion) throws Exception {
                        return new Question(networkQuestion);
                    }
                });

    }

    @Override
    public Maybe<Question> setStringResponseForQuestion(String response, Question question) {
        //any syncing with the api can be done from here if necessary
        if ((response == null) || response.isEmpty()){
            return Maybe.error(new BadResponseException());
        }

        question.setResponse(response);
        return loadNextQuestion(question);
    }

    @Override
    public Maybe<Question> setNumberResponseForQuestion(Double response, Question question) {
        //any syncing with the api can be done from here if necessary
        if(response == null){
            return Maybe.error(new BadResponseException());
        }
        question.setResponse(Double.valueOf(response).toString());
        return loadNextQuestion(question);
    }

    @Override
    public Maybe<Question> setChoicesResponseForQuestion(List<Integer> choice_indicies, Question question) {
        if ((choice_indicies == null) || (choice_indicies.size() == 0)){
            return Maybe.error(new BadResponseException());
        }
        //any syncing with the api can be done from here if necessary
        StringBuilder sb = new StringBuilder();
        if (choice_indicies != null){
            for (int i : choice_indicies){
                sb.append(question.getChoices().get(i));
            }
        }
        question.setResponse(sb.toString());
        return loadNextQuestion(question);
    }

    private Maybe<Question> loadNextQuestion(Question q){
        completedQuestionsLookup.put(q.getId(), q);
        return networkStream
                .elementAt(completedQuestionsLookup.size())
                .map(new Function<NetworkQuestion, Question>() {
                    @Override
                    public Question apply(NetworkQuestion networkQuestion) throws Exception {
                        //this is where we append the label to display at the top
                        Question question = new Question(networkQuestion);
                        if(completedQuestionsLookup.get(0) != null){
                            question.setWelcome("Hi "+completedQuestionsLookup.get(0).getResponse()+"! Let’s talk about...");
                        }
                        //next we need to analyse the NetworkQuestion
                        question.setDialogText(checkForDialog(networkQuestion));
                        return question;
                    }
                });
    }

    private String checkForDialog(NetworkQuestion networkQuestion){
        if((networkQuestion.getTake_aways() == null) || (networkQuestion.getTake_aways().size() == 0)){
            return null;
        }
        for (NetworkTakeAway takeAway : networkQuestion.getTake_aways()){
            boolean conditionsMet = true;
            for (NetworkCondition condition :takeAway.getConditions()){
                String userResponse = completedQuestionsLookup.get(condition.getQuestion_id()).getResponse();
                Double d = Double.valueOf(userResponse);

                if((condition.getGreater_than() != null) &&(condition.getGreater_than() >= d)){
                    Log.v("avtar-logger", "setting conditions met to false because: "+condition.getGreater_than()+"<="+d);
                    conditionsMet = false;
                }
                if((condition.getLess_than() != null) &&(condition.getLess_than() <= d)){
                    Log.v("avtar-logger", "setting conditions met to false because: "+condition.getLess_than()+">="+d);
                    conditionsMet = false;
                }
                if((condition.getEqual_to() != null) &&(condition.getEqual_to().doubleValue() != d)){
                    Log.v("avtar-logger", "setting conditions met to false because: "+condition.getEqual_to()+"!="+d);
                    conditionsMet = false;
                }
            }
            if(conditionsMet){
                return takeAway.getText();
            }
        }
        return null;
    }
}

