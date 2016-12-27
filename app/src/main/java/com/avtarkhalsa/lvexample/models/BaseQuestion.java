package com.avtarkhalsa.lvexample.models;

import com.avtarkhalsa.lvexample.views.QuestionView;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public abstract class BaseQuestion implements QuestionView.ViewModel {
    String questionLabel;
    protected QuestionType questionType;

    protected BaseQuestion(String label){
        questionLabel = label;
    }

    public String getLabel() {
        return questionLabel;
    }
    public QuestionType getType(){
        return questionType;
    }
}
