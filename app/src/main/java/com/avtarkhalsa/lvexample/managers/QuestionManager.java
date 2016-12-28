package com.avtarkhalsa.lvexample.managers;

import com.avtarkhalsa.lvexample.models.Question;

import java.util.List;

import io.reactivex.Maybe;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public interface QuestionManager {
    class BadResponseException extends Exception{};

    Maybe<Question> loadFirstQuestion();

    Maybe<Question> setStringResponseForQuestion(String response, Question question);

    Maybe<Question> setNumberResponseForQuestion(Double response, Question question);

    Maybe<Question> setChoicesResponseForQuestion(List<Integer> choice_indicies, Question question);
}
