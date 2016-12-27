package com.avtarkhalsa.lvexample.models;

import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.views.QuestionView;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class Question implements QuestionView.ViewModel {
    String questionLabel;
    QuestionType questionType;
    String response;

    public Question(NetworkQuestion nq){
        questionLabel = nq.getQuestion_label();
        questionType = QuestionType.fromString(nq.getQuestion_type());
    }

    public String getLabel() {
        return questionLabel;
    }
    public QuestionType getType(){
        return questionType;
    }

    public void setStringResponse(String response){

    }

    public void setNumberResponse(double response){

    }

    public void setChoices(int[] choice_index){
        
    }
}
