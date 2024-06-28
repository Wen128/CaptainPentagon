package com.example.captainpentagon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Environment;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanActivity extends AppCompatActivity {

    ImageButton btnToHome;
    static TextView scanDetail;

    private static final String SERVER_URL = "https://malware-detect-e2fec6effe08.herokuapp.com/analyze"; // Update with your server address
    private static final int REQUEST_PERMISSION = 123;
    private static List<String> scanResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);

        scanDetail = findViewById(R.id.scanDetails);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            scanAPK();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("tag", "Permission granted");
                scanAPK();
            } else {
                Log.d("tag", "Permission denied");
            }
        }
    }

    public List<File> extractApkFiles() {
        List<File> apkFiles = new ArrayList<>();
        File externalStorage = Environment.getExternalStorageDirectory(); // General external storage directory
        findApkFiles(externalStorage, apkFiles);
        Log.d("second", apkFiles.toString());
        return apkFiles;
    }

    private void findApkFiles(File directory, List<File> apkFiles) {
        scanDetail.setText("Searching in directory: " + directory.getAbsolutePath());
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || (file.isFile() && file.getName().toLowerCase().endsWith(".apk"));
            }
        });

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findApkFiles(file, apkFiles); // Recursive call for directories
                } else {
                    apkFiles.add(file); // Add APK file to the list
                    scanDetail.setText("Found APK: " + file.getAbsolutePath());
                }
            }
        } else {
            Log.d("findApkFiles", "No files found in directory: " + directory.getAbsolutePath());
        }
    }

    private static void uploadApkFile(File apkFile, CountDownLatch latch) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1000, TimeUnit.SECONDS)
                    .writeTimeout(1000, TimeUnit.SECONDS)
                    .readTimeout(1000, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", apkFile.getName(),
                            RequestBody.create(MediaType.parse("application/vnd.android.package-archive"), apkFile))
                    .build();

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(requestBody)
                    .build();
            Log.d("tag", "requestBody");

            try {
                scanDetail.setText("Analyzing " + apkFile.getName());

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                Log.d("uploadApkFile", "Uploaded " + apkFile.getName() + " successfully");

                if (response.body().string().contains("malware")) {
                    scanResults.add(apkFile.getName());
                }
                Log.d("result", scanResults.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                latch.countDown(); // Decrement the count of the latch
            }
        }).start();
    }

    public void scanAPK() {
        Log.d("tag", "try");
        List<File> apkFiles = extractApkFiles();
        CountDownLatch latch = new CountDownLatch(apkFiles.size());
        ExecutorService executor = Executors.newFixedThreadPool(4); // Adjust the pool size based on your needs

        for (File apkFile : apkFiles) {
            executor.execute(() -> uploadApkFile(apkFile, latch));
        }

        new Thread(() -> {
            try {
                latch.await(); // Wait for all uploads to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                Intent intent = new Intent(ScanActivity.this, ScanResultActivity.class);
                intent.putStringArrayListExtra("scanResults", new ArrayList<>(scanResults));
                startActivity(intent);
            });
            executor.shutdown();
        }).start();
    }
}
