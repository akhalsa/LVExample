package com.avtarkhalsa.lvexample.models;

/**
 * Created by avtarkhalsa on 12/25/16.
 */
public enum QuestionType {
    Textual("textual"),
    Numerical("numerical"),
    MultiSelect("multi select"),
    SingleSelect("single select");

    private String textLabel;

    QuestionType(String label){
        textLabel = label;
    }


    public static QuestionType fromString(String text) {
        if (text != null) {
            for (QuestionType qt : QuestionType.values()) {
                if (text.equalsIgnoreCase(qt.textLabel)) {
                    return qt;
                }
            }
        }
        return null;
    }
}
