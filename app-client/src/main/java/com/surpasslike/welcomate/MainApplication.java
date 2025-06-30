package com.surpasslike.welcomate;

import android.app.Application;
import android.content.Context;

import com.surpasslike.welcomateservice.data.UserRepository;

public class MainApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        // Initialize the UserRepository with the application context
        UserRepository.initialize(this);
    }

    public static Context getContext() {
        return context;
    }
}
