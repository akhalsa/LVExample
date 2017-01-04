package com.avtarkhalsa.lvexample.networkmodels;

import java.util.ArrayList;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class NetworkQuestion {

    String question_type; //POSSIBLE VALUES: Textual, Numerical, Single Select, Multi Select

    String question_label;

    String question_heading;

    int question_id;

    int page_weight;

    ArrayList<String> choices; //may be null

    ArrayList<NetworkTakeAway> take_aways;

    String skip_expression;

    public String getQuestion_type() {
        return question_type;
    }

    public String getQuestion_label() {
        return question_label;
    }

    public ArrayList<String> getChoices() {
        return choices;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public ArrayList<NetworkTakeAway> getTake_aways() {
        return take_aways;
    }
    public String getSkip_expression() {
        return skip_expression;
    }

    public String getQuestionHeading() {
        return question_heading;
    }


    public int getPageWeight() {
        return page_weight;
    }

}
