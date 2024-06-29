package com.example.captainpentagon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout newslayout;
    private ScrollView scrollView;
    private ProgressBar progressBar;
    private OkHttpClient client;
    private HashSet<String> newsUrls;
    private HashSet<String> imageUrls;
    private HashSet<String> titleList;
    private int currentPage = 1; // Current page number
    private final int PAGE_SIZE = 5; // Number of news items per page
    private boolean isUserScrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.nameView);
        // Get SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
// Retrieve data
        String name = sharedPreferences.getString("name", "User123");

        textView.setText(name);

        ImageButton imageButton1 = findViewById(R.id.imageButton1);
        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        ImageButton imageButton3 = findViewById(R.id.imageButton3);

        imageButton1.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
            startActivity(intent);
        });

        imageButton2.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PageChat.class);
            startActivity(intent);
        });

        imageButton3.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QuizSplashActivity.class);
            startActivity(intent);
        });

        newslayout = findViewById(R.id.newslayout);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize OkHttpClient
        client = new OkHttpClient();
        // Initialize HashSet for tracking news URLs and image URLs
        newsUrls = new HashSet<>();
        imageUrls = new HashSet<>();
        titleList = new HashSet<>();


        // Fetch initial news items from API
        fetchNewsItems(currentPage);

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                isUserScrolling = true;
                if (!v.canScrollVertically(1)) { // Scrolled to the bottom
                    fetchMoreNewsItems();
                }
                if (!v.canScrollVertically(-1) && !v.isPressed()) { // Scrolled to the top and user released
                    refreshNewsItems();
                }
            }
        });

        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                isUserScrolling = false;
            }
            return false;
        });

        // Override the back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Create a confirmation dialog
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Exit")
                        .setMessage("Are you sure you want to quit?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Finish the activity
                            finishAffinity();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss the dialog
                            dialog.dismiss();
                        })
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }



    private void refreshNewsItems() {
        Log.d("RefreshNewsItems", "Refreshing news items...");

        // Show the ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Use a Handler to delay the clearing and refetching process
        new Handler().postDelayed(() -> {
            // Clear only the news-related views (CardViews) from the layout
            for (int i = newslayout.getChildCount() - 1; i >= 0; i--) {
                View child = newslayout.getChildAt(i);
                if (child instanceof CardView) {
                    newslayout.removeViewAt(i);
                }
            }
            // Clear HashSet
            newsUrls.clear();
            imageUrls.clear();
            currentPage = 1; // Reset page number

            // Hide the ProgressBar after 1 second
            progressBar.setVisibility(View.GONE);

            // Fetch news items again
            fetchNewsItems(currentPage);
        }, 1000); // 1000 milliseconds = 1 second
    }

    private void fetchNewsItems(int page) {
        String apiKey = "64c038f63e4c4d159c4e00e2f6a0fc7b"; // Replace with your News API key
        String query = "malware OR trojan OR spyware OR ransomware OR phishing OR cybercrime";
        String language = "en";

        int pageSize = PAGE_SIZE; // Number of news items per page
        String url = String.format("https://newsapi.org/v2/everything?q=%s&language=%s&sortBy=publishedAt&page=%d&pageSize=%d&apiKey=%s",
                query, language, page, pageSize, apiKey);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Update UI to show an error message
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray newsArray = jsonObject.getJSONArray("articles");

                        runOnUiThread(() -> {
                            for (int i = 0; i < newsArray.length(); i++) {
                                try {
                                    JSONObject newsObject = newsArray.getJSONObject(i);
                                    String imageUrl = newsObject.getString("urlToImage");
                                    String title = newsObject.getString("title");
                                    String url = newsObject.getString("url");

                                    // Check if the news URL has already been added, if the title is [Removed], or if the image URL is empty
                                    // Also check if the image URL has already been added
                                    if (!titleList.contains(title) &&!newsUrls.contains(url) && !imageUrls.contains(imageUrl) && !title.equals("[Removed]") && imageUrl != null && !imageUrl.isEmpty()) {
                                        newsUrls.add(url);
                                        imageUrls.add(imageUrl);
                                        titleList.add(title);
                                        addNewsItem(imageUrl, title, url);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                    });
                }
            }
        });
    }

    private void fetchMoreNewsItems() {
        Log.d("FetchMoreNewsItems", "FetchMoreNews...");
        currentPage++; // Increase page number
        fetchNewsItems(currentPage);
    }

    private void addNewsItem(String imageUrl, String newsTitle, final String newsUrl) {
        CardView cardView = new CardView(HomeActivity.this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(45, 30, 45, 30);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(30); // Rounded corners
        cardView.setContentPadding(20, 45, 20, 10); // Padding
        // cardView.setCardBackgroundColor(Color.parseColor("#E9E8E8"));

        LinearLayout messageLayout = new LinearLayout(HomeActivity.this);
        messageLayout.setOrientation(LinearLayout.VERTICAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageButton imageButton = new ImageButton(HomeActivity.this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
        );
        imageButton.setLayoutParams(imageParams);
        imageButton.setScaleType(ImageButton.ScaleType.CENTER_CROP);

        // Use Glide to load the image
        Glide.with(HomeActivity.this).load(imageUrl).into(imageButton);

        TextView textView = new TextView(HomeActivity.this);
        textView.setText(newsTitle);
        textView.setTextSize(16);
        textView.setPadding(10, 10, 10, 10);

        messageLayout.addView(imageButton);
        messageLayout.addView(textView);

        cardView.addView(messageLayout);

        // Set the OnClickListener for the CardView
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
            startActivity(intent);
        });

        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
            startActivity(intent);
        });

        newslayout.addView(cardView);
    }

}