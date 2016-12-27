package com.avtarkhalsa.lvexample.managers;

import com.avtarkhalsa.lvexample.models.Question;

import io.reactivex.Maybe;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public interface QuestionManager {
    Maybe<Question> loadNextQuestion();
}
