package com.avtarkhalsa.lvexample.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.avtarkhalsa.lvexample.LVExampleApplication;
import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.models.Question;
import com.avtarkhalsa.lvexample.models.QuestionPage;
import com.avtarkhalsa.lvexample.views.QuestionView;


import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    @Inject
    QuestionManager questionManager;

    @BindView(R.id.main_coordinator)
    CoordinatorLayout mainCoordinator;

    @BindView(R.id.main_questions_list)
    LinearLayout questionsLinearLayout;

    @BindView(R.id.next_button)
    Button nextButton;
    private QuestionPage currentQuestions;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String invalidInputText = "Invalid Input";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LVExampleApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        nextButton.setEnabled(false);
        questionManager.loadNextQuestions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionMaybeObserver);


    }

    public void nextClicked(View v){
        //first we would populate all the questions using their associated question view
        int count = questionsLinearLayout.getChildCount();
        Question hasTakeAway = null;
        for(int i = 0; i<count; i++){
            QuestionView qv = (QuestionView) questionsLinearLayout.getChildAt(i);
            Question q = currentQuestions.getQuestions().get(i);
            try{
                questionManager.setQuestionResponseWithQuestionView(q, qv);
                if(q.getDialogText() != null){
                    hasTakeAway = q;
                }
            } catch (QuestionManager.BadResponseException bre){
                Toast.makeText(MainActivity.this, invalidInputText, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if(hasTakeAway != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(hasTakeAway.getDialogText());
            builder.setPositiveButton(hasTakeAway.getDialogActionText(), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        questionManager
                .loadNextQuestions()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(questionMaybeObserver);
    }

    private SingleObserver<QuestionPage> questionMaybeObserver = new SingleObserver<QuestionPage>() {
        @Override
        public void onSubscribe(Disposable d) {}

        @Override
        public void onSuccess(QuestionPage questionPage) {
            //this is the code to run any time we get a new question
            //if we were using RetroLambda we would want to use a method reference instead

            currentQuestions = questionPage;
            nextButton.setEnabled(true);
            /*questionView.bindToQuestion(question);
            EditText currentInput = questionView.getCurrentInput();*/
            if(questionPage.hasBackButton()){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }else{
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }
            /*now we need to first remove all children from the main layout
             */
            questionsLinearLayout.removeAllViews();
            for(Question q : questionPage.getQuestions()){
                QuestionView qv = new QuestionView(MainActivity.this);
                qv.bindToQuestion(q);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                qv.setOrientation(LinearLayout.VERTICAL);
                qv.setLayoutParams(params);
                questionsLinearLayout.addView(qv);
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
