package com.avtarkhalsa.lvexample.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.avtarkhalsa.lvexample.LVExampleApplication;
import com.avtarkhalsa.lvexample.R;
import com.avtarkhalsa.lvexample.managers.QuestionManager;
import com.avtarkhalsa.lvexample.models.BaseQuestion;
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;
import com.avtarkhalsa.lvexample.views.QuestionView;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    @Inject
    QuestionManager questionManager;

    @BindView(R.id.question_view)
    QuestionView questionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LVExampleApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        loadQuestion();

    }
    private void loadQuestion(){
        questionManager.loadNextQuestion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseQuestion>() {
                    @Override
                    public void accept(BaseQuestion baseQuestion) throws Exception {
                        renderQuestion(baseQuestion);
                    }
                });
    }
    private void renderQuestion(BaseQuestion bq){
        questionView.bindToQuestion(bq);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadQuestion();
            }
        }, 5000);
    }

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
