package com.avtarkhalsa.lvexample.managers;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.views.QuestionView;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public interface QuestionManager {


    class BadResponseException extends Exception{};

    class EndOfListReachedException extends Exception{};

    Single<List<Question>> loadNextQuestions();

    Single<List<Question>> popQuestionPage(List<Question> currentQuestions);

    void setQuestionResponseWithQuestionView(Question q, QuestionView qv) throws BadResponseException;
}
