package com.example.captainpentagon;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

public class ScanResultActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 123;
    private Button clearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        // Get the scan results and file paths from the intent
        List<String> scanResults = getIntent().getStringArrayListExtra("scanResults");
        List<String> apkFilePaths = getIntent().getStringArrayListExtra("apkFilePaths");

        // Get the results container
        LinearLayout resultsContainer = findViewById(R.id.resultsContainer);

        clearBtn = findViewById(R.id.clearBtn);

        // Get the screen width in pixels
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Calculate the width for TextView and ImageButton
        int textViewWidth = (int) (screenWidth * 0.75);
        int imageButtonWidth = (int) (screenWidth * 0.10);
        int imageButtonHeight = imageButtonWidth;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }

        // Display the results
        if (scanResults != null && !scanResults.isEmpty()) {
            for (int i = 0; i < scanResults.size(); i++) {
                String result = scanResults.get(i);
                String filePath = apkFilePaths.get(i);

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
                actionButton.setImageResource(R.drawable.delete); // Use appropriate drawable

// Set the scale type to fit the image within the button bounds
                actionButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

// Remove padding if any
                actionButton.setPadding(0, 0, 0, 0);

// Set a transparent background to avoid default button styling
                actionButton.setBackgroundColor(Color.TRANSPARENT);

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        60,
                        60);
                buttonParams.gravity = Gravity.END; // Align ImageButton to the right
                buttonParams.setMargins(0, 25, 0, 25); // Remove any margins
                actionButton.setLayoutParams(buttonParams);

                clearBtn.setOnClickListener(v -> deleteAllFiles(apkFilePaths, resultsContainer, scanResults));


                // Set click listener for ImageButton to delete the APK file
                actionButton.setOnClickListener(v -> {
                    File file = new File(filePath);
                    if (file.exists()) {
                        if (file.delete()) {
                            // Remove the result and path from the lists
                            scanResults.remove(result);
                            apkFilePaths.remove(filePath);

                            // Remove the view
                            resultsContainer.removeView(resultLayout);

                            // Show a success toast message
                            Toast.makeText(this, "Deleted: " + result, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to delete: " + result, Toast.LENGTH_SHORT).show();
                            Log.e("FileDeletion", "Failed to delete file: " + filePath);
                        }
                    } else {
                        Toast.makeText(this, "File not found: " + result, Toast.LENGTH_SHORT).show();
                        Log.e("FileDeletion", "File not found: " + filePath);
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
            clearBtn.setVisibility(View.GONE);
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


        }

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

    private void deleteAllFiles(List<String> apkFilePaths, LinearLayout resultsContainer, List<String> scanResults) {
        for (int i = apkFilePaths.size() - 1; i >= 0; i--) {
            String filePath = apkFilePaths.get(i);
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    // Remove the layout view
                    LinearLayout layoutToRemove = (LinearLayout) resultsContainer.getChildAt(i);
                    resultsContainer.removeView(layoutToRemove);
                    // Remove from lists
                    scanResults.remove(i);
                    apkFilePaths.remove(i);
                } else {
                    Log.e("FileDeletion", "Failed to delete file: " + filePath);
                }
            } else {
                Log.e("FileDeletion", "File not found: " + filePath);
            }
        }
        Toast.makeText(this, "All files deleted, Your device is safe!", Toast.LENGTH_SHORT).show();
    }



}
