package com.avtarkhalsa.lvexample.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.avtarkhalsa.lvexample.models.Question;

import java.util.List;

/**
 * Created by avtarkhalsa on 1/2/17.
 */
public class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.QuestionViewHolder>{
    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(QuestionViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void updateQuestionList(List<Question> newQuestions){

    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        public QuestionViewHolder(View itemView) {
            super(itemView);
        }
    }
}
