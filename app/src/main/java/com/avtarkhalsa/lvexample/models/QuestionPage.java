package com.avtarkhalsa.lvexample.models;

import java.util.List;

/**
 * Created by avtarkhalsa on 1/2/17.
 */
public class QuestionPage {
    List<Question> questions;
    int pageWeight;
    boolean hasBackButton;

    public QuestionPage(List<Question> questions, int page, boolean back){
        this.questions = questions;
        pageWeight = page;
        hasBackButton = back;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getPageWeight() {
        return pageWeight;
    }

    public boolean hasBackButton() {
        return hasBackButton;
    }

}
