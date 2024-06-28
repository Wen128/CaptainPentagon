package com.example.captainpentagon;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

public class ScanResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        // Get the scan results from the intent
        List<String> scanResults = getIntent().getStringArrayListExtra("scanResults");

        // Get the results container
        LinearLayout resultsContainer = findViewById(R.id.resultsContainer);

        // Get the screen width in pixels
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Calculate the width for TextView and ImageButton
        int textViewWidth = (int) (screenWidth * 0.75);
        int imageButtonWidth = (int) (screenWidth * 0.10);
        int imageButtonHeight = imageButtonWidth;

        // Display the results
        if (scanResults != null && !scanResults.isEmpty()) {
            for (String result : scanResults) {
                // Create a new LinearLayout for each result
                LinearLayout resultLayout = new LinearLayout(this);
                resultLayout.setOrientation(LinearLayout.HORIZONTAL);
                resultLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Create a TextView for the result
                TextView resultTextView = new TextView(this);
                resultTextView.setText(result);
                resultTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        textViewWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Create an ImageButton for the result
                ImageButton actionButton = new ImageButton(this);
                actionButton.setImageResource(R.drawable.errormeter); // Use appropriate drawable
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        imageButtonWidth,
                        imageButtonHeight);
                buttonParams.gravity = Gravity.END; // Align ImageButton to the right
                actionButton.setLayoutParams(buttonParams);

                // Set click listener for ImageButton
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle click event
                        Intent intent = new Intent(ScanResultActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

                // Add TextView and ImageButton to the result layout
                resultLayout.addView(resultTextView);
                resultLayout.addView(actionButton);

                // Add the result layout to the container
                resultsContainer.addView(resultLayout);
            }
        } else {
            // Handle the case when no results are found
            TextView noResultTextView = new TextView(this);
            noResultTextView.setText("No malware detected.");
            noResultTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            resultsContainer.addView(noResultTextView);
        }

        View safemeter = findViewById(R.id.safemeter);
        View errormeter = findViewById(R.id.errormeter);
        TextView softwareTextView = findViewById(R.id.software);

        if (scanResults != null && !scanResults.isEmpty()) {
            safemeter.setVisibility(View.GONE);
            errormeter.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) softwareTextView.getLayoutParams();
            layoutParams.topToBottom = R.id.errormeter;
            softwareTextView.setLayoutParams(layoutParams);
        } else {
            safemeter.setVisibility(View.VISIBLE);
            errormeter.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) softwareTextView.getLayoutParams();
            layoutParams.topToBottom = R.id.safemeter;
            softwareTextView.setLayoutParams(layoutParams);

            // Override the back button press
            OnBackPressedCallback callback = new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Handle the back button event
                    Intent intent = new Intent(ScanResultActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, callback);
        }


    }

}
