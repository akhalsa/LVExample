package com.avtarkhalsa.lvexample.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.models.QuestionType;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by avtarkhalsa on 12/26/16.
 */
public class QuestionView extends LinearLayout {
    public interface ViewModel{
        String getLabel();
        QuestionType getType();
        List<String> getChoices();
        String getWelcome();
    }

    @BindView(R.id.question_label)
    TextSwitcher question_label;

    @BindView(R.id.question_welcome)
    TextView welcome_label;

    @BindView(R.id.numerical_input)
    EditText numericalInput;

    @BindView(R.id.textual_input)
    EditText textualInput;

    @BindView(R.id.single_select_input)
    RadioGroup singleSelectInput;

    @BindView(R.id.multi_select_input)
    RecyclerView multiSelectInput;

    private MultiSelectAdapter currentMultiSelectAdapter;

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void bindToQuestion(ViewModel vm){
        //lets animate the question transition
        question_label.setText(vm.getLabel());
        hideAllInputs();
        clearAllInputs();
        switch (vm.getType()){
            case Textual:
                textualInput.setVisibility(View.VISIBLE);
                break;
            case Numerical:
                numericalInput.setVisibility(View.VISIBLE);
                break;
            case SingleSelect:
                singleSelectInput.setVisibility(View.VISIBLE);
                populateSingleSelect(vm);
                break;
            case MultiSelect:
                multiSelectInput.setVisibility(View.VISIBLE);
                populateMultiSelect(vm);
                break;

        }
        if(vm.getWelcome() != null){
            welcome_label.setVisibility(View.VISIBLE);
            welcome_label.setText(vm.getWelcome());
        }
    }

    public EditText getCurrentInput(){
        if(textualInput.getVisibility() == View.VISIBLE){
            return textualInput;
        }else if (numericalInput.getVisibility() == View.VISIBLE) {
            return numericalInput;
        }else{
            return null;
        }
    }

    public String getTextInput(){
        if (textualInput.getText().toString().isEmpty()){
            return null;
        }
        return textualInput.getText().toString();
    }

    public Double getNumericalInput(){
        if (numericalInput.getText().toString().isEmpty()){
            return null;
        }
        return Double.valueOf(numericalInput.getText().toString());
    }

    public List<Integer> getSingleSelection(){
        if (singleSelectInput.getCheckedRadioButtonId() == -1){
            return null;
        }
        List<Integer>checks =  new ArrayList<>();
        checks.add(singleSelectInput.getCheckedRadioButtonId());
        return checks;
    }

    public List<Integer> getMultiSelections(){
        if (currentMultiSelectAdapter == null){
            return null;
        }
        List<Integer> checked = currentMultiSelectAdapter.getSelections();
        if(checked.size() == 0){
            return null;
        }
        return checked;
    }

    private void init() {
        inflate(getContext(), R.layout.question_view_layout, this);
        ButterKnife.bind(this);
        multiSelectInput.setLayoutManager(new LinearLayoutManager(getContext()));
        question_label.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                return new TextView(getContext());
            }
        });
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        question_label.setInAnimation(in);
        question_label.setOutAnimation(out);
        hideAllInputs();
    }

    private void populateSingleSelect(ViewModel vm){
        for (int i =0; i<vm.getChoices().size(); i++){
            String choice = vm.getChoices().get(i);
            RadioButton rb = new RadioButton(this.getContext());
            rb.setText(choice);
            rb.setId(i);
            singleSelectInput.addView(rb);
        }
    }

    private void populateMultiSelect(ViewModel vm){
        currentMultiSelectAdapter = new MultiSelectAdapter(vm.getChoices());
        multiSelectInput.setAdapter(currentMultiSelectAdapter);
    }
    private void hideAllInputs(){
        numericalInput.setVisibility(View.GONE);
        textualInput.setVisibility(View.GONE);
        singleSelectInput.setVisibility(View.GONE);
        multiSelectInput.setVisibility(View.GONE);
        welcome_label.setVisibility(View.GONE);
    }

    private void clearAllInputs(){
        singleSelectInput.clearCheck();
        singleSelectInput.removeAllViews();
        numericalInput.setText("");
        textualInput.setText("");
        multiSelectInput.setAdapter(null);
        currentMultiSelectAdapter = null;
    }
}
