package com.avtarkhalsa.lvexample.views;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.models.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by avtarkhalsa on 1/2/17.
 */
public class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.QuestionViewHolder>{

    private List<Question> questions;
    private QuestionManager questionManager;
    private HashMap<Question, String> cache;
    public QuestionListAdapter(QuestionManager qm){
        questionManager = qm;
        cache = new HashMap<>();
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_view_layout, parent, false);

        return new QuestionViewHolder(inflatedView);

    }

    @Override
    public void onBindViewHolder(QuestionViewHolder holder, int position) {
        holder.bindToQuestion(questions.get(position));
    }

    @Override
    public int getItemCount() {
        if(questions == null){
            return 0;
        }
        return questions.size();
    }

    public void updateQuestionList(List<Question> newQuestions){
        questions = newQuestions;
        notifyDataSetChanged();
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.question_label)
        TextView question_label;

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

        private Question currentQuestion;
        public QuestionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            multiSelectInput.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            hideAllInputs();
        }

        public void bindToQuestion(Question q){
            cacheCurrentInput();
            currentQuestion = q;
            question_label.setText(currentQuestion.getLabel());
            hideAllInputs();
            clearAllInputs();
            switch (currentQuestion.getType()) {
                case Textual:
                    textualInput.setVisibility(View.VISIBLE);
                    break;
                case Numerical:
                    numericalInput.setVisibility(View.VISIBLE);
                    break;
                case SingleSelect:
                    singleSelectInput.setVisibility(View.VISIBLE);
                    populateSingleSelect(currentQuestion);
                    break;
                case MultiSelect:
                    multiSelectInput.setVisibility(View.VISIBLE);
                    populateMultiSelect(currentQuestion);
                    break;
            }
        }

        private void cacheCurrentInput(){
            if(currentQuestion != null){
                try{
                    cache.put(currentQuestion, questionManager.getStringEncoding(currentQuestion, this));
                } catch (QuestionManager.BadResponseException e){
                }
            }
        }
        private void populateSingleSelect(Question question){
            for (int i =0; i<question.getChoices().size(); i++){
                String choice = question.getChoices().get(i);
                RadioButton rb = new RadioButton(singleSelectInput.getContext());
                rb.setText(choice);
                rb.setId(i);
                singleSelectInput.addView(rb);
            }
        }

        private void populateMultiSelect(Question question){
            currentMultiSelectAdapter = new MultiSelectAdapter(question.getChoices());
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

        public EditText getTextInput(){
            return textualInput;
        }

        public EditText getNumericalInput(){
            return numericalInput;
        }

        public List<Integer> getSingleSelection(){
            List<Integer>checks =  new ArrayList<>();
            if (singleSelectInput.getCheckedRadioButtonId() == -1){
                return checks;
            }
            checks.add(singleSelectInput.getCheckedRadioButtonId());
            return checks;
        }

        public List<Integer> getMultiSelections(){
            List<Integer>checks =  new ArrayList<>();
            if (currentMultiSelectAdapter == null){
                return checks;
            }
            checks = currentMultiSelectAdapter.getSelections();
            return checks;
        }

    }
}
