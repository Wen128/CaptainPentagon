package com.example.captainpentagon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AppCompatActivity;

public class QuizMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_main);

        TextView easyButton = findViewById(R.id.easy_button);
        TextView mediumButton = findViewById(R.id.normal_button);
        TextView hardButton = findViewById(R.id.hard_button);
        ImageView app_icon = findViewById(R.id.app_icon);



        easyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz("easy");
            }
        });

        mediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz("normal");
            }
        });

        hardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz("hard");
            }
        });

        // Override the back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Intent intent = new Intent(QuizMainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void startQuiz(String difficulty) {
        Intent intent = new Intent(QuizMainActivity.this, QuizActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }


}