package com.example.captainpentagon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput;
    private Spinner spinnerField;
    private Button NextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameInput = findViewById(R.id.nameInput);
        spinnerField = findViewById(R.id.spinner_field);
        NextButton = findViewById(R.id.NextButton);

        // Create an ArrayAdapter using the custom layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Select_your_field, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_item);

        // Apply the adapter to the spinner
        spinnerField.setAdapter(adapter);

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String filteredText = s.toString().replaceAll("[^a-zA-Z\\s]", "");
                if (!filteredText.equals(s.toString())) {
                    nameInput.setText(filteredText);
                    nameInput.setSelection(filteredText.length());
                }
            }
        });

        NextButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String selectedField = spinnerField.getSelectedItem().toString();

            if (name.isEmpty() && selectedField.equals("Select your field")) {
                Toast.makeText(MainActivity.this, "Please complete all options", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                // Get SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                // Create an editor
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // Store data
                editor.putString("name", name);
                editor.putString("level", selectedField);
                editor.putBoolean("setup", true);

                // Commit the changes
                editor.apply();
                startActivity(intent);
            }
        });
    }
}