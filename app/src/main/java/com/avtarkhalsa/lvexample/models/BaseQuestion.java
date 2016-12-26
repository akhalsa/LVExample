package com.avtarkhalsa.lvexample.models;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class BaseQuestion {
    String questionLabel;

    protected BaseQuestion(String label){
        questionLabel = label;
    }
    public String getQuestionLabel() {
        return questionLabel;
    }
}
