package com.mishazharov.alarmrace;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity {
    String DEBUG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.main_title));

        // Makes sure that challenge code is typed in all caps. Just an aesthetic thing
        ((EditText) findViewById(R.id.challenge_code)).setFilters(new InputFilter[]{new InputFilter.AllCaps()});;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
