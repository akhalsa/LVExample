package com.avtarkhalsa.lvexample.activities;

import android.os.Bundle;
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
import com.avtarkhalsa.lvexample.networking.APIInterface;
import com.avtarkhalsa.lvexample.networkmodels.NetworkQuestion;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    @Inject
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LVExampleApplication) getApplication()).getAPIComponent().inject(this);
        setContentView(R.layout.activity_main);
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
        apiInterface.getAllQuestions()
                .subscribe(new Consumer<List<NetworkQuestion>>(){
                    @Override
                    public void accept(List<NetworkQuestion> networkQuestions) throws Exception {
                        Log.v("avtar-logger", "we got network questions count: "+networkQuestions.size());
                    }
                });
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
