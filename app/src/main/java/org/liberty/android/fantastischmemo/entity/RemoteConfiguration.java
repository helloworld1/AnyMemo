package org.liberty.android.fantastischmemo.entity;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import javax.inject.Inject;

public class RemoteConfiguration {

    private static final String TAG = RemoteConfiguration.class.getSimpleName();

    private static final String ANYMEMO_WEB_SERVICE_ENDPOINT_KEY = "anymemo_web_service_endpoint";

    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Inject
    public RemoteConfiguration(FirebaseRemoteConfig firebaseRemoteConfig) {
        this.firebaseRemoteConfig = firebaseRemoteConfig;
    }

    public String getAnyMemoWebServiceEndpoint() {
        String endpoint = firebaseRemoteConfig.getString(ANYMEMO_WEB_SERVICE_ENDPOINT_KEY);
        Log.v(TAG, "Remote config AnyMemoWebService endpoint: " + endpoint);
        return endpoint;
    }
}
