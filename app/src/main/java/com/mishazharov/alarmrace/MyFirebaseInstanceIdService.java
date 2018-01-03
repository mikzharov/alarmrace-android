package com.mishazharov.alarmrace;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Misha on 12/31/2017.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private final String DEBUG_TAG = "MyFirebaseInstanceIDService.java";
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(DEBUG_TAG, "Refreshed token: " + refreshedToken);
    }
}
