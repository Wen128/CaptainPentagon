package com.example.captainpentagon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        TextView softwareTextView = findViewById(R.id.software); // 假设这是你要调整的TextView

        if (scanResults != null && !scanResults.isEmpty()) {
            // 当有扫描结果时，显示错误仪表盘，隐藏安全仪表盘
            safemeter.setVisibility(View.GONE);
            errormeter.setVisibility(View.VISIBLE);
            // 设置TextView的约束，使其顶部与errormeter的底部对齐
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) softwareTextView.getLayoutParams();
            layoutParams.topToBottom = R.id.errormeter;
            softwareTextView.setLayoutParams(layoutParams);
        } else {
            // 当没有扫描结果时，显示安全仪表盘，隐藏错误仪表盘
            safemeter.setVisibility(View.VISIBLE);
            errormeter.setVisibility(View.GONE);
            // 设置TextView的约束，使其顶部与safemeter的底部对齐
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

}
