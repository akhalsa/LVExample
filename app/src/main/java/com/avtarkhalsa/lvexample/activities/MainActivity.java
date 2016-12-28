package com.avtarkhalsa.lvexample.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.avtarkhalsa.lvexample.LVExampleApplication;
import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.views.QuestionView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    @Inject
    QuestionManager questionManager;

    @BindView(R.id.question_view)
    QuestionView questionView;

    private Question currentQuestion;

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
                .subscribe(questionConsumer);


    }

    public void nextClicked(View v){
        //first we need to retrieve the current value from the View
        switch(currentQuestion.getType()){
            case Textual:
                String response = questionView.getTextInput();
                questionManager
                        .setStringResponseForQuestion(response, currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionConsumer);
                break;
            case Numerical:
                Double doubleResponse = questionView.getNumericalInput();
                questionManager
                        .setNumberResponseForQuestion(doubleResponse, currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionConsumer);
                break;
            case SingleSelect:
                questionManager
                        .setChoicesResponseForQuestion(questionView.getSingleSelection(), currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionConsumer);
                break;
            case MultiSelect:
                questionManager
                        .setChoicesResponseForQuestion(questionView.getMultiSelections(), currentQuestion)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(questionConsumer);
                break;
        }
    }

    private Consumer<Question> questionConsumer = new Consumer<Question>() {
        @Override
        public void accept(Question question) throws Exception {
            //this is the code to run any time we get a new question
            //if we were using RetroLambda we would want to use a method reference instead
            questionView.bindToQuestion(question);
            currentQuestion = question;
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
