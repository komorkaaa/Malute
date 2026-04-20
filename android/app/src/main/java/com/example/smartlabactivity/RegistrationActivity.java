package com.example.smartlabactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.UserRepository;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

public class RegistrationActivity extends AppCompatActivity {

    private static final String PREF_EMAIL = "Email";
    private static final String PREF_USER_ID = "user_id";

    private SharedPreferences settings;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button nextButton;

    private UserRepository userRepository;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        settings = getSharedPreferences("app_settings", MODE_PRIVATE);
        userRepository = new UserRepository();

        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        nextButton = findViewById(R.id.btnSubmit);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled() && !isRegistering) {
                registerUser();
            }
        });

        updateButtonState();
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        isRegistering = true;
        nextButton.setEnabled(false);
        nextButton.setText("Регистрация...");

        userRepository.registerUser(email, password, new UserRepository.RegisterCallback() {
            @Override
            public void onSuccess(UserRecordResponse user) {
                runOnUiThread(() -> {
                    SharedPreferences.Editor prefEditor = settings.edit();
                    prefEditor.putString(PREF_EMAIL, email);
                    prefEditor.putString(PREF_USER_ID, user.id);
                    prefEditor.apply();

                    Toast.makeText(RegistrationActivity.this,
                            "Регистрация успешна!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegistrationActivity.this, EmailCaptchaActivity.class);
                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);

                    isRegistering = false;
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(RegistrationActivity.this,
                            "Ошибка: " + error, Toast.LENGTH_LONG).show();

                    nextButton.setEnabled(true);
                    nextButton.setText("Далее");
                    isRegistering = false;
                });
            }
        });
    }

    private void updateButtonState() {
        if (!isRegistering) {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            boolean isValid = isValidEmail(email) && isValidPassword(password);
            nextButton.setEnabled(isValid);
        }

        nextButton.setBackgroundResource(R.drawable.button_enable);
        nextButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8;
    }
}