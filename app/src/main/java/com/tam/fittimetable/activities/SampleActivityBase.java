package com.tam.fittimetable.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base launcher activity, to handle most of the common plumbing for samples.
 */
public class SampleActivityBase extends AppCompatActivity {

    public static final String TAG = "SampleActivityBase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected  void onStart() {
        super.onStart();
       // initializeLogging();
    }
}
