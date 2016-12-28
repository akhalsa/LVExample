package com.avtarkhalsa.lvexample.managers;

import android.util.Log;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkCondition;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.networkmodels.NetworkTakeAway;
import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 1);
    Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 1);
    Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);
    Operator LESS_THAN = new Operator("<", 2, Operator.Associativity.LEFT, 1);
    Operator GREATER_THAN = new Operator(">", 2, Operator.Associativity.LEFT, 1);
    SimpleBooleanEvaluator sbe;

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
        Parameters params = new Parameters();
        params.add(AND);
        params.add(OR);
        params.add(NEGATE);
        params.add(LESS_THAN);
        params.add(GREATER_THAN);
        params.addExpressionBracket(BracketPair.PARENTHESES);
        sbe = new SimpleBooleanEvaluator(params);
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
                        question.setDialogText(evalForDialog(networkQuestion));
                        return question;
                    }
                });
    }

    private class SimpleBooleanEvaluator extends DoubleEvaluator{
        public SimpleBooleanEvaluator(Parameters p){
            super(p);
        }
        @Override
        protected Double evaluate(Operator operator, Iterator<Double> operands, Object evaluationContext) {
            //ok booleans will have to be handled as 1 or 0
            //well have to manually handle all the operators but each one should ONLY return a 1.0: True or a 0.0: False
            if(operator == AND){
                Double o1 = operands.next();
                Double o2 = operands.next();
                //return 0 if either o1 or o2 is 0
                if((o1 == 0.0) || (o2 == 0.0)){
                    return 0.0;
                }else{
                    return 1.0;
                }
            }else if(operator == OR){
                Double o1 = operands.next();
                Double o2 = operands.next();
                //return 1.0 of either o1 does not equal 0.0 or if o2 does not equal 0.0
                if((o1 != 0.0) || (o2 != 0.0)){
                    return 1.0;
                }else{
                    return 0.0;
                }
            } else if(operator == LESS_THAN){
                Double o1 = operands.next();
                Double o2 = operands.next();
                //these should be actual doubles thoguh a side effect is that false is less than true
                if(o1 < o2){
                    return 1.0;
                }else{
                    return 0.0;
                }
            } else if(operator == GREATER_THAN) {
                Double o1 = operands.next();
                Double o2 = operands.next();
                if (o1 > o2) {
                    return 1.0;
                } else {
                    return 0.0;
                }
            }
            return 0.0;
        }
    }

    private String evalForDialog(NetworkQuestion networkQuestion){

        if(networkQuestion.getTake_aways() != null){
            for (NetworkTakeAway nta : networkQuestion.getTake_aways()){
                String expression = nta.getLogic();
                //First we want to identify all the CHECK_SELECTION(qId, Index) conditions
                //We will manually evaluate these guys and replace them with the EVAL_TRUE or EVAL_FALSE
                //String constants below. This is a little hacky, but it will let us treat CHECK_SELECTION
                //as a method which will get swapped out with an expression that the SBE will handle correctly

                String EVAL_TRUE = "(0 < 1)";
                String EVAL_FALSE = "(0 > 1)";


                //ok next lets find all the integer values referenced here and replace them
                Pattern p = Pattern.compile("\\$(\\d+)");
                Matcher m = p.matcher(expression);
                HashMap<String, String> replacements = new HashMap<>();
                while(m.find()){
                    Integer id = Integer.valueOf(m.group(1));
                    String newVal = Double.valueOf(completedQuestionsLookup.get(id).getResponse()).toString();
                    replacements.put(m.group(1), newVal);
                }

                for(String qId : replacements.keySet()){
                    Pattern single = Pattern.compile("\\$"+qId);
                    expression = single.matcher(expression).replaceAll(replacements.get(qId));
                }
                if (sbe.evaluate(expression) == 1.0){
                    //we have a match!
                    return nta.getText();
                }
            }
        }
        return null;
    }
}

