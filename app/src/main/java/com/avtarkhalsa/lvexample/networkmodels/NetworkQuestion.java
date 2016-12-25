package com.avtarkhalsa.lvexample.networkmodels;

import java.util.ArrayList;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public class NetworkQuestion {

    String question_type; //POSSIBLE VALUES: Textual, Numerical, Single Select, Multi Select

    String question_label;

    ArrayList<String> choices; //may be null
}
