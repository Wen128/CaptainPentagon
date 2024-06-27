package com.example.captainpentagon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PageChat extends AppCompatActivity {

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_chat);

        // Retrieve data from the Intent
        Intent intent = getIntent();
       SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
// Retrieve data
            String userName = sharedPreferences.getString("name", "User123");

        // Display welcome message
        TextView textView = findViewById(R.id.pagemainchat);
        textView.setText("Hi " + userName + ", I am Mr.Bot. You can ask me any question.");

        // Delay for 2 seconds (2000 milliseconds) before moving to AndroidChat
        new Handler().postDelayed(() -> {
            Intent androidChatIntent = new Intent(PageChat.this, AndroidChat.class);

            androidChatIntent.putExtra("NAME", userName); // Pass the user name to AndroidChat
            startActivity(androidChatIntent);
            finish(); // Finish PageChat activity so user can't go back to it
        }, 2000);
}
}
