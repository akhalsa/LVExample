package com.avtarkhalsa.lvexample.models;

import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.views.QuestionView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class Question implements QuestionView.ViewModel {
    String questionLabel;
    QuestionType questionType;
    String response;
    List<String> choices;

    public Question(NetworkQuestion nq){
        questionLabel = nq.getQuestion_label();
        questionType = QuestionType.fromString(nq.getQuestion_type());
        choices = new ArrayList<>(nq.getChoices());
        response = null; //perhaps the responses will be synced against the server at some point. For now lets leave them local
    }

    public String getLabel() {
        return questionLabel;
    }
    public QuestionType getType(){
        return questionType;
    }

    public void setStringResponse(String response){
        this.response = response;
    }

    public void setNumberResponse(double response){
        this.response = Double.valueOf(response).toString();
    }

    public void setChoices(int[] choice_indicies){
        StringBuilder sb = new StringBuilder();
        for (int i : choice_indicies){
            sb.append(choices.get(i));
        }
        response = sb.toString();
    }
}
