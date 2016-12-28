package com.avtarkhalsa.lvexample.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.avtarkhalsa.lvexample.views.QuestionView;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    @Inject
    QuestionManager questionManager;

    @BindView(R.id.question_view)
    QuestionView questionView;

    @BindView(R.id.main_coordinator)
    CoordinatorLayout mainCoordinator;

    @BindView(R.id.next_button)
    Button nextButton;
    private Question currentQuestion;

    private String invalidInputText = "Invalid Input";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LVExampleApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        questionManager.loadFirstQuestion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionMaybeObserver);


    }

    public void nextClicked(View v){
        //first we need to retrieve the current value from the View
        switch(currentQuestion.getType()){
            case Textual:
                String response = questionView.getTextInput();
                questionManager
                        .setStringResponseForQuestion(response, currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionMaybeObserver);
                break;
            case Numerical:
                Double doubleResponse = questionView.getNumericalInput();
                questionManager
                        .setNumberResponseForQuestion(doubleResponse, currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionMaybeObserver);
                break;
            case SingleSelect:
                questionManager
                        .setChoicesResponseForQuestion(questionView.getSingleSelection(), currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionMaybeObserver);
                break;
            case MultiSelect:
                questionManager
                        .setChoicesResponseForQuestion(questionView.getMultiSelections(), currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionMaybeObserver);
                break;
        }
    }
    private MaybeObserver<Question> questionMaybeObserver = new MaybeObserver<Question>() {
        @Override
        public void onSubscribe(Disposable d) {}

        @Override
        public void onSuccess(Question question) {
            //this is the code to run any time we get a new question
            //if we were using RetroLambda we would want to use a method reference instead
            questionView.bindToQuestion(question);
            EditText currentInput = questionView.getCurrentInput();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if(currentInput != null){
                imm.showSoftInput(currentInput, InputMethodManager.SHOW_IMPLICIT);
                currentInput.requestFocus();
                currentInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT){
                            Log.v("avtar-logger", "got a next event");
                            nextClicked(nextButton);
                            return true;
                        }else{
                            return false;
                        }
                    }
                });
            }else{
                imm.hideSoftInputFromWindow(mainCoordinator.getWindowToken(), 0);
            }
            currentQuestion = question;
        }

        @Override
        public void onError(Throwable e) {
            if(e instanceof QuestionManager.BadResponseException){
                Toast.makeText(MainActivity.this, invalidInputText, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onComplete() {}
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
