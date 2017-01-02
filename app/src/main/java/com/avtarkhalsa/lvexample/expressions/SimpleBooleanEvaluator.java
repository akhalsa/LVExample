package com.avtarkhalsa.lvexample.expressions;

import com.avtarkhalsa.lvexample.models.Question;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by avtarkhalsa on 1/2/17.
 */
public class SimpleBooleanEvaluator extends DoubleEvaluator {
    static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 1);
    static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 1);
    static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);
    static Operator LESS_THAN = new Operator("<", 2, Operator.Associativity.LEFT, 1);
    static Operator GREATER_THAN = new Operator(">", 2, Operator.Associativity.LEFT, 1);

    public static final String EVAL_TRUE = "(0 < 1)";
    public static final String EVAL_FALSE = "(0 > 1)";

    public static Parameters getParams(){
        Parameters params = new Parameters();
        params.add(AND);
        params.add(OR);
        params.add(NEGATE);
        params.add(LESS_THAN);
        params.add(GREATER_THAN);
        params.addExpressionBracket(BracketPair.PARENTHESES);
        return params;
    }
    public SimpleBooleanEvaluator(){
        super(getParams());
    }
    @Override
    protected Double evaluate(Operator operator, Iterator<Double> operands, Object evaluationContext) {
        //ok booleans will have to be handled as 1 or 0
        //well have to manually handle all the operators but each one should ONLY return a 1.0: True or a 0.0: False
        if(operator == AND){
            Double o1 = operands.next();
            Double o2 = operands.next();
            //return 0 if either o1 or o2 is 0
            if((o1 == 0.0) || (o2 == 0.0)){
                return 0.0;
            }else{
                return 1.0;
            }
        }else if(operator == OR){
            Double o1 = operands.next();
            Double o2 = operands.next();
            //return 1.0 of either o1 does not equal 0.0 or if o2 does not equal 0.0
            if((o1 != 0.0) || (o2 != 0.0)){
                return 1.0;
            }else{
                return 0.0;
            }
        } else if(operator == LESS_THAN){
            Double o1 = operands.next();
            Double o2 = operands.next();
            //these should be actual doubles thoguh a side effect is that false is less than true
            if(o1 < o2){
                return 1.0;
            }else{
                return 0.0;
            }
        } else if(operator == GREATER_THAN) {
            Double o1 = operands.next();
            Double o2 = operands.next();
            if (o1 > o2) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
        return 0.0;
    }

    public boolean evaluateExpression(String expression, HashMap<Integer, Question> answeredQuestions){
        //First we want to identify all the CHECK_SELECTION(qId, Index) conditions
        //We will manually evaluate these guys and replace them with the EVAL_TRUE or EVAL_FALSE
        //String constants below. This is a little hacky, but it will let us treat CHECK_SELECTION
        //as a method which will get swapped out with an expression that the SBE will handle correctly
        Pattern p = Pattern.compile("CHECK_SELECTION\\((.*?)\\)");
        Matcher m = p.matcher(expression);
        HashMap<String, String> replacements = new HashMap<>();
        while(m.find()){
            String[] methodParams = m.group(1).split(",");
            Integer qId = Integer.valueOf(methodParams[0]);
            String check = methodParams[1].trim();
            if(!answeredQuestions.containsKey(qId)){
                return false;
            }
            if(Arrays.asList(answeredQuestions.get(qId).getResponse().split(",")).contains(check)){
                replacements.put(m.group(1), SimpleBooleanEvaluator.EVAL_TRUE);
            }else{
                replacements.put(m.group(1), SimpleBooleanEvaluator.EVAL_FALSE);
            }
        }
        for(String params : replacements.keySet()){
            p = Pattern.compile("CHECK_SELECTION\\("+params+"\\)");
            expression = p.matcher(expression).replaceAll(replacements.get(params));
        }

        //ok next lets find all the integer values referenced here and replace them
        p = Pattern.compile("\\$(\\d+)");
        m = p.matcher(expression);
        replacements = new HashMap<>();
        while(m.find()){
            Integer id = Integer.valueOf(m.group(1));
            if(!answeredQuestions.containsKey(id)){
                return false;
            }
            String newVal = Double.valueOf(answeredQuestions.get(id).getResponse()).toString();
            replacements.put(m.group(1), newVal);
        }

        for(String qId : replacements.keySet()){
            p = Pattern.compile("\\$"+qId);
            expression = p.matcher(expression).replaceAll(replacements.get(qId));
        }
        if (evaluate(expression) == 1.0){
            //we have a match!
            return true;
        }
        return false;
    }
}