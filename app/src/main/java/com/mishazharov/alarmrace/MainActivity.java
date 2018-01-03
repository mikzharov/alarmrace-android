package com.mishazharov.alarmrace;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

public class MainActivity extends AppCompatActivity {
    String DEBUG_TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseRemoteConfig mRemoteConfig;
    private void checkGoogleApi(){
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int code = availability.isGooglePlayServicesAvailable(getApplicationContext());
        if(code != ConnectionResult.SUCCESS){
            availability.makeGooglePlayServicesAvailable(this);
        }
    }
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
        checkGoogleApi();
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

    @Override
    public void onResume(){
        super.onResume();
        checkGoogleApi();
    }
    public void startGame(View v){
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        EditText code_view = findViewById(R.id.challenge_code);
                        String code = code_view.getText().toString().toUpperCase();

                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams params = new RequestParams();

                        // This is the game invite
                        params.put("gameCode", code);

                        // Send a UID so it can be kept in the database
                        params.put("uid", currentUser.getUid());

                        String token = FirebaseInstanceId.getInstance().getToken();
                        if (token != null && !token.isEmpty()) {
                            // Send a FCM token so that a notification can be sent to the user
                            params.put("fcm_token", token);
                        }

                        client.addHeader("Authorization", task.getResult().getToken());
                        Log.d(DEBUG_TAG, "Url target: " + mRemoteConfig.getString("url_api_root") + mRemoteConfig.getString("url_api_game_start"));
                        client.post(getApplicationContext(), mRemoteConfig.getString("url_api_root") + mRemoteConfig.getString("url_api_game_start"), params, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Log.d(DEBUG_TAG, "Got statuscode " + statusCode);
                                Log.d(DEBUG_TAG, new String(responseBody));
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Log.d(DEBUG_TAG, "Failure, Got statuscode " + statusCode);
                                Log.d(DEBUG_TAG, new String(responseBody));
                            }
                        });
                    } else {
                        Log.d(DEBUG_TAG, "Could not get token");
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e(DEBUG_TAG, e.toString());
                        }
                        Crashlytics.logException(task.getException());
                    }
                }
            });
        }
    }

    public void createGame(View v){

    }
}
