package com.example.captainpentagon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AndroidChat extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;
    private String userName;
    private OkHttpClient client;
    private static final String API_URL = "https://qwen.pythonanywhere.com/ask";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private boolean aiFirstMessageSent = false; // Track if AI's first message is sent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve data from the Intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("NAME");
        // Initialize views and variables
        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);
        ImageButton btnHome = findViewById(R.id.btnHome); // Initialize the home button
        client = new OkHttpClient();

        // Send initial message from AI when activity starts
        if (!aiFirstMessageSent) {
            sendInitialMessageFromAI();
        }

        // Set click listener for Send button
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // Add user message to chat view
                addMessageToChat("User", message);

                // Clear the message input field
                etMessage.setText("");

                // Send user message to API
                sendMessageToAPI(message, "User");
            } else {
                Toast.makeText(AndroidChat.this, "Please enter your message", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for Home button
        btnHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(AndroidChat.this, HomeActivity.class); // Replace HomeActivity with your actual homepage activity
            startActivity(homeIntent);
            finish(); // Optionally finish the current activity
        });
    }

    // Method to send initial message from AI
    private void sendInitialMessageFromAI() {
        aiFirstMessageSent = true; // Mark AI's first message as sent

        // Construct initial greeting message from AI with user's name
        String initialMessage = "Hi，" + userName + ", what can I help you with?";
        // Send initial message to API
        sendMessageToAPI(initialMessage, "AI");
    }

    @SuppressLint("SetTextI18n")
    private void addMessageToChat(String sender, String message) {
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        messageLayout.setPadding(5, 20, 5, 20);

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(20);
        textView.setPadding(40, 20, 40, 10);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(textParams);

        if (sender.equals("AI")) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    200, // width
                    200 // height
            ));
            imageView.setImageResource(R.drawable.logoai); // Set AI's avatar

            messageLayout.setGravity(Gravity.START);
            messageLayout.addView(imageView);
            messageLayout.addView(textContainer);
            textParams.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.ai_message_background);
        } else {
            messageLayout.setGravity(Gravity.END);
            messageLayout.addView(textContainer);
            textParams.gravity = Gravity.END;
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setBackgroundResource(R.drawable.user_message_background);
        }

        textContainer.addView(textView);
        chatContainer.addView(messageLayout);

        // Scroll to the bottom after adding a new message
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessageToAPI(String message, String sender) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("question", message);
            jsonObject.put("name", userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    addMessageToChat("Error", e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String reply = jsonResponse.getString("response");
                        runOnUiThread(() -> {
                            addMessageToChat("AI", reply);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            addMessageToChat("Error", "Error parsing JSON");
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        addMessageToChat("Error", "Unable to get a response from the server");
                    });
                }
            }
   });
}
}
