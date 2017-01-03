package com.avtarkhalsa.lvexample.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avtarkhalsa.lvexample.LVExampleApplication;
import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.views.QuestionListAdapter;
import com.avtarkhalsa.lvexample.views.QuestionView;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.MaybeObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    @Inject
    QuestionManager questionManager;

    @BindView(R.id.question_recycler)
    RecyclerView questionRecycler;

    @BindView(R.id.main_coordinator)
    CoordinatorLayout mainCoordinator;

    @BindView(R.id.next_button)
    Button nextButton;
    private List<Question> currentQuestions;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String invalidInputText = "Invalid Input";

    private QuestionListAdapter questionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LVExampleApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        questionListAdapter = new QuestionListAdapter();
        questionRecycler.setAdapter(questionListAdapter);
        questionRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        nextButton.setEnabled(false);
        questionManager.loadNextQuestions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionMaybeObserver);


    }

    public void nextClicked(View v){
        //first we would populate all the questions using their associated question view

        questionManager
                .loadNextQuestions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionMaybeObserver);
    }

    private SingleObserver<List<Question>> questionMaybeObserver = new SingleObserver<List<Question>>() {
        @Override
        public void onSubscribe(Disposable d) {}

        @Override
        public void onSuccess(List<Question> questions) {
            //this is the code to run any time we get a new question
            //if we were using RetroLambda we would want to use a method reference instead

            questionListAdapter.updateQuestionList(questions);

            /*if(question.getDialogText() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(question.getDialogText());
                builder.setPositiveButton(question.getDialogActionText(), null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }*/
            nextButton.setEnabled(true);
            /*questionView.bindToQuestion(question);
            EditText currentInput = questionView.getCurrentInput();*/

            if(question.getCanGoBack()){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }else{
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            /*if(currentInput != null){
                imm.showSoftInput(currentInput, InputMethodManager.SHOW_IMPLICIT);
                currentInput.requestFocus();
                currentInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT){
                            //nextClicked(nextButton);
                            return true;
                        }else{
                            return false;
                        }
                    }
                });
            }else{
                imm.hideSoftInputFromWindow(mainCoordinator.getWindowToken(), 0);
            }*/
            currentQuestions = questions;
        }

        @Override
        public void onError(Throwable e) {
            if(e instanceof QuestionManager.BadResponseException){
                Toast.makeText(MainActivity.this, invalidInputText, Toast.LENGTH_SHORT).show();
            }else if (e instanceof  QuestionManager.EndOfListReachedException){
                Toast.makeText(MainActivity.this, "YOU HAVE REACHED THE END OF THE QUESTION LIST", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                questionManager.popQuestionPage(currentQuestions)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionMaybeObserver);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
