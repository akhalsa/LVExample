package com.avtarkhalsa.lvexample.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.models.QuestionType;

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
    }

    @BindView(R.id.question_label)
    TextView question_label;

    @BindView(R.id.numerical_input)
    EditText numericalInput;

    @BindView(R.id.textual_input)
    EditText textualInput;

    @BindView(R.id.single_select_input)
    RadioGroup singleSelectInput;

    @BindView(R.id.multi_select_input)
    RecyclerView multiSelectInput;

    private ViewModel viewModel;

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.question_view_layout, this);
        ButterKnife.bind(this);
        multiSelectInput.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void bindToQuestion(ViewModel vm){
        question_label.setText(vm.getLabel());
        hideAllInputs();
        viewModel = vm;
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
    }

    private void populateSingleSelect(ViewModel vm){
        for (int i =0; i<vm.getChoices().size(); i++){
            String choice = vm.getChoices().get(i);
            RadioButton rb = new RadioButton(this.getContext());
            rb.setText(choice);
            singleSelectInput.addView(rb);
        }
    }

    private void populateMultiSelect(ViewModel vm){
        multiSelectInput.setAdapter(new MultiSelectAdapter(vm.getChoices()));
    }
    private void hideAllInputs(){
        numericalInput.setVisibility(View.GONE);
        textualInput.setVisibility(View.GONE);
        singleSelectInput.removeAllViews();
        singleSelectInput.setVisibility(View.GONE);
        multiSelectInput.setVisibility(View.GONE);
    }


}
