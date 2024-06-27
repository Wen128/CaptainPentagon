package com.example.captainpentagon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class QuizSplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // Splash screen timeout duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_splash); // Set the splash screen layout

        // Delay for SPLASH_TIME_OUT milliseconds before starting MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity
                Intent intent = new Intent(QuizSplashActivity.this, QuizMainActivity.class);
                startActivity(intent);

                // Close SplashActivity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
