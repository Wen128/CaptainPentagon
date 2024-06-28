package com.example.captainpentagon;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ScanResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        TextView resultTextView = findViewById(R.id.resultTextView);

        // Get the scan results from the intent
        List<String> scanResults = getIntent().getStringArrayListExtra("scanResults");

        // Display the results
        if (scanResults != null && !scanResults.isEmpty()) {
            StringBuilder results = new StringBuilder();
            for (String result : scanResults) {
                results.append(result).append("\n");
            }
            resultTextView.setText(results.toString());
        } else {
            resultTextView.setText("No malware detected.");
        }

        View safemeter = findViewById(R.id.safemeter);
        View errormeter = findViewById(R.id.errormeter);

        if (scanResults != null && !scanResults.isEmpty()) {
            safemeter.setVisibility(View.GONE);
            errormeter.setVisibility(View.VISIBLE);
        } else {
            safemeter.setVisibility(View.VISIBLE);
            errormeter.setVisibility(View.GONE);
        }
    }
}
