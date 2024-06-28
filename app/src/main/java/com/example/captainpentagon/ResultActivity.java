package com.example.captainpentagon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView resultTextView, feedbackTextView, nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        resultTextView = findViewById(R.id.result_text);
        feedbackTextView = findViewById(R.id.feedback_text);
        nameTextView = findViewById(R.id.name_text);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView resultImageView = findViewById(R.id.resultImageView);


        // Get the name and score from the intent
        Intent intent = getIntent();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
// Retrieve data
        String name = sharedPreferences.getString("name", "User123");
        int score = intent.getIntExtra("score", 0);

        // Set the name and score text
        nameTextView.setText("Hello, " + name + "!");
        resultTextView.setText("You scored " + score + " /10");

        // Provide feedback based on the score
        String feedback;
        if (score <= 4) {
            resultImageView.setImageResource(R.drawable.aiwrong);
            feedback = "Keep Improving!!";
        } else if (score <= 7) {
            resultImageView.setImageResource(R.drawable.ya);
            feedback = "Good Attempt!";
        } else {
            resultImageView.setImageResource(R.drawable.ya);
            feedback = "Excellent!!!";
        }
        feedbackTextView.setText(feedback);
    }
}