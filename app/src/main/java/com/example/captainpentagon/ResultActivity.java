package com.example.captainpentagon;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView resultTextView, feedbackTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultTextView = findViewById(R.id.result_text);
        feedbackTextView = findViewById(R.id.feedback_text);

        TextView textView = findViewById(R.id.name_text);
        // Get SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
// Retrieve data
        String name = sharedPreferences.getString("name", "User123");

        textView.setText(name);


        // Get the name and score from the intent
        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);

        // Set the name and score text
        resultTextView.setText("You scored " + score + " /10");

        // Provide feedback based on the score
        String feedback;
        if (score <= 2) {
            feedback = "Disappointed! You have many things to improve in Malware";
        } else if (score <= 4) {
            feedback = "Quite bad! Your knowledge in Malware is not good. Try again";
        } else if (score <= 6) {
            feedback = "You knowledge in Malware but you can do it better.";
        } else if (score <= 8) {
            feedback = "Well Done! You are good in Malware";
        } else {
            feedback = "Well Done! Looks like you are a pro in Malware";
        }
        feedbackTextView.setText(feedback);
}
}