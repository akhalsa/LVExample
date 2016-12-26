package com.avtarkhalsa.lvexample.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.models.BaseQuestion;
import com.avtarkhalsa.lvexample.models.QuestionType;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by avtarkhalsa on 12/26/16.
 */
public class QuestionView extends LinearLayout {
    public interface ViewModel{
        String getLabel();
        QuestionType getType();
    }

    @BindView(R.id.question_label)
    TextView question_label;

    @BindView(R.id.numerical_input)
    EditText numericalInput;

    @BindView(R.id.textual_input)
    EditText textualInput;

    private ViewModel viewModel;

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.question_view_layout, this);
        ButterKnife.bind(this);
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
        }
    }

    private void hideAllInputs(){
        numericalInput.setVisibility(View.GONE);
        textualInput.setVisibility(View.GONE);

    }
}
