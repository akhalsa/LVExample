package com.avtarkhalsa.lvexample.networkmodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by avtarkhalsa on 12/28/16.
 */
public class NetworkTakeAway {
    String text;
    String action_button;
    String expression;

    public String getText() {
        return text;
    }
    public String getLogic() {
        return expression;
    }
    public String getAction_button() {
        return action_button;
    }


}
