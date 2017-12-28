package com.mishazharov.alarmrace;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

public class MainActivity extends AppCompatActivity {
    String DEBUG_TAG = "MainActivity";
    private FirebaseAuth mAuth;

    void signIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(DEBUG_TAG, "Anonymous success is successful");
                    } else {
                        Log.d(DEBUG_TAG, "Anonymous success is not successful");
                    }
                }
            });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.main_title));

        // Makes sure that challenge code is typed in all caps. Just an aesthetic thing
        ((EditText) findViewById(R.id.challenge_code)).setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // It seems that no matter what I do, google keyboard still shows suggestions
        ((EditText) findViewById(R.id.challenge_code)).setInputType(TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public void onStart() {
        super.onStart();
        signIn();
    }

    public void startGame(View v){
        EditText code_view = (EditText) findViewById(R.id.challenge_code);
        String code = code_view.getText().toString().toUpperCase();
    }

    public void createGame(View v){

    }
}
