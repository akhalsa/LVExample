package com.avtarkhalsa.lvexample.managers;

import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.models.QuestionPage;
import com.avtarkhalsa.lvexample.views.QuestionListAdapter;

import io.reactivex.Single;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public interface QuestionManager {


    class BadResponseException extends Exception{};

    class EndOfListReachedException extends Exception{};

    Single<QuestionPage> loadNextQuestions();

    Single<QuestionPage> popQuestionPage(QuestionPage currentPage);
    String getStringEncoding(Question q, QuestionListAdapter.QuestionViewHolder qvh) throws BadResponseException;
    void setQuestionResponse(Question q, String Response);
}
