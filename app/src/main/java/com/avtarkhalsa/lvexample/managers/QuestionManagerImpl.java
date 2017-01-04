package com.avtarkhalsa.lvexample.managers;

import android.util.Log;

import com.avtarkhalsa.lvexample.expressions.SimpleBooleanEvaluator;
import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.models.QuestionPage;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.networkmodels.NetworkTakeAway;
import com.avtarkhalsa.lvexample.views.QuestionView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class QuestionManagerImpl implements QuestionManager {

    SimpleBooleanEvaluator simpleBooleanEvaluator;

    private ReplaySubject<NetworkQuestion> networkStream;

    private HashMap<Integer, Question> completedQuestionsLookup;
    private int currentQuestionWeight;
    boolean finished;

    public QuestionManagerImpl(APIInterface api, SimpleBooleanEvaluator sbe, HashMap<Integer, Question> completed, int startingWeight){
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
        completedQuestionsLookup = completed;
        currentQuestionWeight = startingWeight;
        finished = false;
        simpleBooleanEvaluator = sbe;
    }

    //under retroLamdb / java8 these would be method references, but in java7 its easier to hold them as object references
    private Predicate<NetworkQuestion> removeSkippableQuestions = new Predicate<NetworkQuestion>() {
        @Override
        public boolean test(NetworkQuestion networkQuestion) throws Exception {
            if((networkQuestion.getSkip_expression() != null) &&
                    (simpleBooleanEvaluator.evaluateExpression(networkQuestion.getSkip_expression(), completedQuestionsLookup))){
                return false;
            }
            return true;
        }
    };

    private Predicate<NetworkQuestion> removeQuestionsNotOnCurrentPage = new Predicate<NetworkQuestion>() {
        @Override
        public boolean test(NetworkQuestion networkQuestion) throws Exception {
            if(networkQuestion.getPageWeight() != currentQuestionWeight){
                return false;
            }
            return true;
        }
    };

    private Predicate<NetworkQuestion> removeCompletedQuestions = new Predicate<NetworkQuestion>() {
        @Override
        public boolean test(NetworkQuestion networkQuestion) throws Exception {
            if(completedQuestionsLookup.containsKey(networkQuestion.getQuestion_id())){
                return false;
            }
            return true;
        }
    };

    private Comparator<NetworkQuestion> sortIntoPages  = new Comparator<NetworkQuestion>() {
        @Override
        public int compare(NetworkQuestion o1, NetworkQuestion o2) {
            return o1.getPageWeight() - o2.getPageWeight();
        }
    };

    private Function<NetworkQuestion, Question> mapNetworkQuestionToQuestion = new Function<NetworkQuestion, Question>() {
        @Override
        public Question apply(NetworkQuestion networkQuestion) throws Exception {
            //this is where we append the label to display at the top
            Question question = new Question(networkQuestion);
            if((networkQuestion.getQuestionHeading() != null) &&
                    (simpleBooleanEvaluator.canSwap(networkQuestion.getQuestionHeading(), completedQuestionsLookup))){
                String sectionHeader = simpleBooleanEvaluator.swapTextValuesIntoExpression(networkQuestion.getQuestionHeading(), completedQuestionsLookup);
                question.setWelcomeLabel(sectionHeader);
            }else{
                question.setWelcomeLabel(null);
            }
            return question;
        }
    };

    private void incrementWeight(){
        try{
            //We throw out everything below the current page including the current page
            //Then we remove any questions we decide we can skip
            //we either have nothing left or the first item is the weight of the new page
            NetworkQuestion firstQuestionOnNextPage = networkStream
                    .sorted(sortIntoPages)
                    .filter(new Predicate<NetworkQuestion>() {
                        @Override
                        public boolean test(NetworkQuestion networkQuestion) throws Exception {
                            if(networkQuestion.getPageWeight() <= currentQuestionWeight){
                                return false;
                            }
                            return true;
                        }
                    })
                    .filter(removeSkippableQuestions)
                    .blockingFirst();
            currentQuestionWeight = firstQuestionOnNextPage.getPageWeight();
        } catch (NoSuchElementException e){
            finished = true;
        }
    }
    private List<NetworkQuestion> remainingQuestionsOnCurrentPage(){
        List<NetworkQuestion> remaining = new ArrayList<>();
        try{
            remaining = networkStream
                    .filter(removeQuestionsNotOnCurrentPage)
                    .filter(removeCompletedQuestions)
                    .toList()
                    .blockingGet();
        }catch (NoSuchElementException nsee){
            Log.v("avtar-logger", "no such elements");
        }
        return remaining;
    }

    private String convertSelectionsToResponse(List<Integer> selections){
        StringBuilder sb = new StringBuilder();
        for(Integer i : selections){
            sb.append(i.toString());
        }
        return sb.toString();
    }

    public void setQuestionResponseWithQuestionView(final Question question, QuestionView questionView) throws BadResponseException{
        switch(question.getType()){
            case Textual:
                String response = questionView.getTextInput();
                if(response == null){
                    throw new BadResponseException();
                }
                question.setResponse(response);
                break;
            case Numerical:
                Double doubleResponse = questionView.getNumericalInput();
                if(doubleResponse == null){
                    throw new BadResponseException();
                }
                question.setResponse(doubleResponse.toString());
                break;
            case SingleSelect:
                List<Integer> selection = questionView.getSingleSelection();
                if(selection == null){
                    throw new BadResponseException();
                }
                question.setResponse(convertSelectionsToResponse(selection));
                break;
            case MultiSelect:
                List<Integer> selections = questionView.getMultiSelections();
                if(selections.size() == 0){
                    throw new BadResponseException();
                }
                question.setResponse(convertSelectionsToResponse(selections));
                break;
        }
        completedQuestionsLookup.put(question.getId(), question);
        String dialogString = null;
        String actionString = null;
        NetworkQuestion networkQuestion = networkStream
                .filter(new Predicate<NetworkQuestion>() {
                    @Override
                    public boolean test(NetworkQuestion networkQuestion) throws Exception {
                        if(question.getId() != networkQuestion.getQuestion_id()){
                            return false;
                        }
                        return true;
                    }
                })
                .blockingFirst();


        if(networkQuestion.getTake_aways() != null) {
            for (NetworkTakeAway nta : networkQuestion.getTake_aways()) {
                Boolean showDialog = simpleBooleanEvaluator.evaluateExpression(nta.getLogic(), completedQuestionsLookup);
                if(showDialog) {
                    dialogString = nta.getText();
                    actionString = nta.getAction_button();
                    break;
                }
            }
        }
        question.setDialogText(dialogString, actionString);
    }

    @Override
    public Single<QuestionPage> loadNextQuestions(){
        //check to make sure the current page is finished
        if(remainingQuestionsOnCurrentPage().size() > 0){
            return Single.defer(new Callable<SingleSource<QuestionPage>>() {
                @Override
                public SingleSource<QuestionPage> call() throws Exception {
                    throw new BadResponseException();
                }
            });
        }
        incrementWeight();
        if (finished) {
            return Single.defer(new Callable<SingleSource<QuestionPage>>() {
                @Override
                public SingleSource<QuestionPage> call() throws Exception {
                    throw new EndOfListReachedException();
                }
            });
        }
        return networkStream
                .subscribeOn(Schedulers.io())
                .filter(removeQuestionsNotOnCurrentPage)
                .filter(removeSkippableQuestions)
                .map(mapNetworkQuestionToQuestion)
                .toList()
                .map(new Function<List<Question>, QuestionPage>() {
                    @Override
                    public QuestionPage apply(List<Question> questions) throws Exception {
                        return new QuestionPage(questions, currentQuestionWeight, completedQuestionsLookup.size() > 0);
                    }
                });

    }
    @Override
    public Single<QuestionPage> popQuestionPage(final QuestionPage questionPage) {
        if(!questionPage.hasBackButton()){
            return Single.defer(new Callable<SingleSource<QuestionPage>>() {
                @Override
                public SingleSource<QuestionPage> call() throws Exception {
                    throw new EndOfListReachedException();
                }
            });
        }

        //we shouldn't need to do a try catch here because we already broke out of the method if the current
        //questionPage didnt have a back button
        currentQuestionWeight = networkStream
                .sorted(sortIntoPages)
                .filter(new Predicate<NetworkQuestion>() {
                    @Override
                    public boolean test(NetworkQuestion networkQuestion) throws Exception {
                        if(networkQuestion.getPageWeight() >= questionPage.getPageWeight()){
                            return false;
                        }
                        return true;
                    }
                })
                .filter(removeSkippableQuestions)
                .blockingLast()
                .getPageWeight();



        return networkStream
                .subscribeOn(Schedulers.io())
                .map(new Function<NetworkQuestion, NetworkQuestion>() {
                    @Override
                    public NetworkQuestion apply(NetworkQuestion networkQuestion) throws Exception {
                        if((networkQuestion.getPageWeight() >= currentQuestionWeight)
                                && (completedQuestionsLookup.containsKey(networkQuestion.getQuestion_id()))){
                            completedQuestionsLookup.remove(networkQuestion.getQuestion_id());
                        }
                        return networkQuestion;
                    }
                })
                .filter(removeQuestionsNotOnCurrentPage)
                .filter(removeSkippableQuestions)
                .map(mapNetworkQuestionToQuestion)
                .toList()
                .map(new Function<List<Question>, QuestionPage>() {
                    @Override
                    public QuestionPage apply(List<Question> questions) throws Exception {
                        return new QuestionPage(questions, currentQuestionWeight, completedQuestionsLookup.size() > 0);
                    }
                });
    }
}

