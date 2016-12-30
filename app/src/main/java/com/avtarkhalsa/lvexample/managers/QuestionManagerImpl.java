package com.avtarkhalsa.lvexample.managers;

import android.util.Log;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.networkmodels.NetworkTakeAway;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
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
    private ArrayList<Question> questionStack;
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
        questionStack = new ArrayList<>();
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
                .subscribeOn(Schedulers.io())
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
            for (Integer i : choice_indicies){
                sb.append(i.toString());
            }
        }
        question.setResponse(sb.toString());
        return loadNextQuestion(question);
    }

    @Override
    public Maybe<Question> popQuestion(Question q){
        //need to remove the most recent question from the list
        if(completedQuestionsLookup.containsKey(q.getId())){
            //the current question was answered so well need to pop it first
            Question qToRemove = questionStack.get(questionStack.size()-1);
            questionStack.remove(qToRemove);
            completedQuestionsLookup.remove(qToRemove.getId());
        }

        return Maybe.just(questionStack.get(questionStack.size()-1));
    }

    private Maybe<Question> loadQuestionWithPosition(int position){
        return networkStream
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<NetworkQuestion>() {
                    @Override
                    public boolean test(NetworkQuestion networkQuestion) throws Exception {
                        if((networkQuestion.getSkip_expression() == null) || (networkQuestion.getSkip_expression().isEmpty())){
                            return true;
                        }
                        if(evalExpression(networkQuestion.getSkip_expression())){
                            return false;
                        }
                        return true;
                    }
                })
                .elementAt(position)
                .map(new Function<NetworkQuestion, Question>() {
                    @Override
                    public Question apply(NetworkQuestion networkQuestion) throws Exception {
                        //this is where we append the label to display at the top
                        Question question = new Question(networkQuestion);
                        if(completedQuestionsLookup.get(0) != null){
                            question.setWelcome("Hi "+completedQuestionsLookup.get(0).getResponse()+"! Let’s talk about...");
                        }

                        //next we need to analyse the NetworkQuestion
                        String dialogString = null;
                        String actionString = null;
                        if(networkQuestion.getTake_aways() != null) {
                            for (NetworkTakeAway nta : networkQuestion.getTake_aways()) {
                                Boolean showDialog = evalExpression(nta.getLogic());
                                if(showDialog) {
                                    dialogString = nta.getText();
                                    actionString = nta.getAction_button();
                                    break;
                                }
                            }

                        }
                        question.setDialogText(dialogString);
                        question.setDialogActionText(actionString);
                        question.setCanGoBack(questionStack.size() > 0);
                        return question;
                    }
                });
    }
    private Maybe<Question> loadNextQuestion(Question q){
        completedQuestionsLookup.put(q.getId(), q);
        questionStack.add(q);
        return loadQuestionWithPosition(completedQuestionsLookup.size());
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

    private Boolean evalExpression(String expression){
        //First we want to identify all the CHECK_SELECTION(qId, Index) conditions
        //We will manually evaluate these guys and replace them with the EVAL_TRUE or EVAL_FALSE
        //String constants below. This is a little hacky, but it will let us treat CHECK_SELECTION
        //as a method which will get swapped out with an expression that the SBE will handle correctly

        String EVAL_TRUE = "(0 < 1)";
        String EVAL_FALSE = "(0 > 1)";
        Pattern p = Pattern.compile("CHECK_SELECTION\\((.*?)\\)");
        Matcher m = p.matcher(expression);
        HashMap<String, String> replacements = new HashMap<>();
        while(m.find()){
            String[] methodParams = m.group(1).split(",");
            Integer qId = Integer.valueOf(methodParams[0]);
            String check = methodParams[1].trim();
            if(!completedQuestionsLookup.containsKey(qId)){
                return false;
            }
            if(Arrays.asList(completedQuestionsLookup.get(qId).getResponse().split(",")).contains(check)){
                replacements.put(m.group(1), EVAL_TRUE);
            }else{
                replacements.put(m.group(1), EVAL_FALSE);
            }
        }
        for(String params : replacements.keySet()){
            p = Pattern.compile("CHECK_SELECTION\\("+params+"\\)");
            expression = p.matcher(expression).replaceAll(replacements.get(params));
        }

        //ok next lets find all the integer values referenced here and replace them
        p = Pattern.compile("\\$(\\d+)");
        m = p.matcher(expression);
        replacements = new HashMap<>();
        while(m.find()){
            Integer id = Integer.valueOf(m.group(1));
            if(!completedQuestionsLookup.containsKey(id)){
                return false;
            }
            String newVal = Double.valueOf(completedQuestionsLookup.get(id).getResponse()).toString();
            replacements.put(m.group(1), newVal);
        }

        for(String qId : replacements.keySet()){
            p = Pattern.compile("\\$"+qId);
            expression = p.matcher(expression).replaceAll(replacements.get(qId));
        }
        if (sbe.evaluate(expression) == 1.0){
            //we have a match!
            return true;
        }
        return false;
    }
}

