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
    int id;
    List<String> choices;

    String welcome;

    String dialogText;

    String dialogActionText;

    public Question(NetworkQuestion nq){
        questionLabel = nq.getQuestion_label();
        questionType = QuestionType.fromString(nq.getQuestion_type());
        id = nq.getQuestion_id();
        if((questionType == QuestionType.MultiSelect) || (questionType == QuestionType.SingleSelect)){
            choices = new ArrayList<>(nq.getChoices());
        }else{
            choices = new ArrayList<>();
        }

        response = null; //perhaps the responses will be synced against the server at some point. For now lets leave them local
        welcome = null;
        dialogText = null;
    }

    //Values Managed by the QuestionManager
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }
    public String getDialogText() {
        return dialogText;
    }
    public void setDialogText(String dialogText) {
        this.dialogText = dialogText;
    }
    public String getDialogActionText() {
        return dialogActionText;
    }

    public void setDialogActionText(String dialogActionText) {
        this.dialogActionText = dialogActionText;
    }
    public int getId() {
        return id;
    }

    // View Model Implementation
    public String getLabel() {
        return questionLabel;
    }
    public QuestionType getType(){
        return questionType;
    }
    public List<String> getChoices(){
        return choices;
    }
    public String getWelcome() {
        return welcome;
    }

}
