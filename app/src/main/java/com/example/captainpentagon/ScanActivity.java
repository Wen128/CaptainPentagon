package com.example.captainpentagon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanActivity extends AppCompatActivity {

    ImageButton btnToHome;
    static TextView scanDetail, percentText;
    Button toQuizBtn;
    private int apkCount = 0; // APK 檔案計數器
    private static final String SERVER_URL = "https://malware-detect-e2fec6effe08.herokuapp.com/analyze"; // Update with your server address
    private static final int REQUEST_PERMISSION = 123;
    private static List<String> scanResults = new ArrayList<>();
    private static final int TARGET_PERCENT = 100; // 目標百分比

    private List<String> apkFilePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);

        scanDetail = findViewById(R.id.scanDetails);
        toQuizBtn = findViewById(R.id.toquizbtn);
        percentText = findViewById(R.id.percentText);

        toQuizBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this, QuizSplashActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Request manage all files access permission
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    storageActivityResultLauncher.launch(intent);
                } catch (Exception e) {
                    Log.e("Error", "Unable to request manage all files access permission", e);
                }
            } else {
                // External storage manager permission already granted
                scanAPK();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request read external storage permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);
            } else {
                // Read external storage permission already granted
                scanAPK();
            }
        } else {
            // No need for permission check on older Android versions
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
                // Handle the case where the permission is denied
                showPermissionDeniedMessage();
            }
        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // External storage manager permission granted
                        scanAPK();
                    } else {
                        // Handle permission denied
                        Log.d("Permission", "Manage all files access permission denied");
                        showPermissionDeniedMessage();
                    }
                }
            }
    );

    private void showPermissionDeniedMessage() {
        runOnUiThread(() -> scanDetail.setText("Permission denied. Cannot scan for APK files."));
    }

    public List<File> extractApkFiles() {
        List<File> apkFiles = new ArrayList<>();
        File externalStorage = Environment.getExternalStorageDirectory(); // General external storage directory
        findApkFiles(externalStorage, apkFiles);
        Log.d("second", apkFiles.toString());

        // 設置提取的 APK 檔案計數
        apkCount = apkFiles.size();
        Log.d("apkCount", "Total APK files found: " + apkCount);

        return apkFiles;
    }

    private void findApkFiles(File directory, List<File> apkFiles) {
        runOnUiThread(() -> scanDetail.setText("Searching in directory: " + directory.getAbsolutePath()));

        File[] files = directory.listFiles(file -> file.isDirectory() || (file.isFile() && file.getName().toLowerCase().endsWith(".apk")));

        if (files != null) {
            List<Thread> threads = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    Thread thread = new Thread(() -> findApkFiles(file, apkFiles));
                    threads.add(thread);
                    thread.start();
                } else {
                    apkFiles.add(file);
                    scanDetail.post(() -> scanDetail.setText("Found " + apkFiles.size() + " APKs in " + directory.getAbsolutePath()));
                }
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadApkFile(File apkFile, CountDownLatch latch) {
        ExecutorService executor = Executors.newFixedThreadPool(8); // Adjust based on your needs

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.SECONDS)
                    .writeTimeout(200, TimeUnit.SECONDS)
                    .readTimeout(200, TimeUnit.SECONDS)
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
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                scanDetail.post(() -> scanDetail.setText("Uploaded " + apkFile.getName() + " successfully"));

                Log.d("uploadApkFile", "Uploaded " + apkFile.getName() + " successfully");

                if (response.body().string().contains("malware")) {
                    scanResults.add(apkFile.getName());
                    apkFilePaths.add(apkFile.getAbsolutePath()); // Store file path
                }
                Log.d("result", scanResults.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                latch.countDown(); // Decrement the count of the latch
                updateUploadProgress(latch.getCount());
            }
        });
        executor.shutdown();
    }

    public void scanAPK() {
        List<File> apkFiles = extractApkFiles();
        CountDownLatch latch = new CountDownLatch(apkFiles.size());
        ExecutorService executor = Executors.newFixedThreadPool(4); // Adjust the pool size based on your needs

        for (File apkFile : apkFiles) {
            executor.execute(() -> {
                uploadApkFile(apkFile, latch);
            });
        }

        new Thread(() -> {
            try {
                latch.await(); // Wait for all uploads to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                percentText.setText(TARGET_PERCENT + "%");
                Intent intent = new Intent(ScanActivity.this, ScanResultActivity.class);
                intent.putStringArrayListExtra("scanResults", new ArrayList<>(scanResults));
                intent.putStringArrayListExtra("apkFilePaths", new ArrayList<>(apkFilePaths)); // Pass file paths
                startActivity(intent);
            });
            executor.shutdown();
        }).start();
    }

    private void updateUploadProgress(long remainingCount) {
        int uploadedCount = apkCount - (int) remainingCount;
        int currentPercent = (int) (((float) uploadedCount / apkCount) * (TARGET_PERCENT)); // 上传阶段更新进度
        runOnUiThread(() -> percentText.setText(currentPercent + "%"));
    }
}
