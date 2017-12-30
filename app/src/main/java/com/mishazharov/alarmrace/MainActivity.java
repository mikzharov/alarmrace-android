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

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONException;

import java.io.IOException;
import java.net.URI;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

public class MainActivity extends AppCompatActivity {
    String DEBUG_TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseRemoteConfig mRemoteConfig;
    private void firebaseConfigFetch(){
        mRemoteConfig.fetch(3600).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mRemoteConfig.activateFetched();
                    Log.d(DEBUG_TAG, "Successfully fetched remote config");
                }else{
                    Log.d(DEBUG_TAG, "Failed to fetch remote config");
                }
            }
        });
    }
    void signIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(DEBUG_TAG, "Anonymous sign in is successful");
                    } else {
                        Log.d(DEBUG_TAG, "Anonymous sign in is not successful");
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
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        mRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        firebaseConfigFetch();
    }
    @Override
    public void onStart() {
        super.onStart();
        signIn();
        firebaseConfigFetch();
    }

    public void startGame(View v){
        mAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()){
                    EditText code_view = (EditText) findViewById(R.id.challenge_code);
                    String code = code_view.getText().toString().toUpperCase();

                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    // params.put("key", "");

                    client.addHeader("Authorization", task.getResult().getToken());
                    client.post(getApplicationContext(), mRemoteConfig.getString("url_api_root") + mRemoteConfig.getString("url_api_game_start"), params, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.d(DEBUG_TAG, "Got statuscode " + statusCode);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d(DEBUG_TAG, "Failure, Got statuscode " + statusCode);
                        }
                    });
                }else{
                    Log.d(DEBUG_TAG, "Could not get token");
                    Exception e = task.getException();
                    Log.d(DEBUG_TAG, e.toString());
                    Crashlytics.logException(task.getException());
                }
            }
        });

    }

    public void createGame(View v){

    }
}
