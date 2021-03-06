package com.avtarkhalsa.lvexample.networkmodels;

import java.util.ArrayList;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class NetworkQuestion {

    String question_type; //POSSIBLE VALUES: Textual, Numerical, Single Select, Multi Select

    String question_label;

    ArrayList<String> choices; //may be null

    public String getQuestion_type() {
        return question_type;
    }

    public String getQuestion_label() {
        return question_label;
    }

    public ArrayList<String> getChoices() {
        return choices;
    }
}
