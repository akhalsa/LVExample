package com.avtarkhalsa.lvexample.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.avtarkhalsa.lvexample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by avtarkhalsa on 12/27/16.
 */
public class MultiSelectAdapter extends RecyclerView.Adapter<MultiSelectAdapter.MultiSelectViewHolder> {

    private List<String> choices;
    private HashMap<Integer, Boolean> selections;

    public MultiSelectAdapter(List<String> choices){
        this.choices = choices;
        selections = new HashMap<>();
    }

    @Override
    public MultiSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_checkbox_item, parent, false);
        return new MultiSelectViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(MultiSelectViewHolder holder, int position) {
        holder.bind(choices.get(position), position);
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    public List<Integer> getSelections(){
        List<Integer> checked = new ArrayList<>();
        for (Integer i : selections.keySet()){
            if (selections.get(i)){
                checked.add(i);
            }
        }
        return checked;
    }

    public class MultiSelectViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_checkbox)
        CheckBox cb;
        @BindView(R.id.checkbox_label)
        TextView tv;

        private int currentPosition;

        public MultiSelectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selections.put(currentPosition, isChecked);
                }
            });
        }

        public void bind(String label, int position){
            currentPosition = position;
            tv.setText(label);
            if(selections.containsKey(position) && selections.get(position)){
                cb.setChecked(true);
            }else{
                cb.setChecked(false);
            }
        }
    }
}
