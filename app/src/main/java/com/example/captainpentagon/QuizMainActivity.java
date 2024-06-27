package com.example.captainpentagon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class QuizMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_main);

        Button easyButton = findViewById(R.id.easy_button);
        Button mediumButton = findViewById(R.id.normal_button);
        Button hardButton = findViewById(R.id.hard_button);
        ImageButton btnHome = findViewById(R.id.btnHome); // Find the ImageButton btnHome

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

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to PageChat activity
                Intent homeIntent = new Intent(QuizMainActivity.this, QuizSplashActivity.class);
                startActivity(homeIntent);
                finish(); // Optionally finish the current activity
            }
        });
    }

    private void startQuiz(String difficulty) {
        Intent intent = new Intent(QuizMainActivity.this, QuizActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}