package com.example.smartlabactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.UserRepository;
import com.example.smartlabactivity.api.dto.TokenResponse;

public class AuthActivity extends AppCompatActivity {
    private static final String PROVIDER_VK = "vk";
    private static final String PROVIDER_YANDEX = "yandex";
    private static final String PREF_EMAIL = "Email";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_TOKEN = "auth_token";
    private static final String PREF_FIRST_NAME = "profile_first_name";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button nextButton;
    private TextView registerTextView;
    private ImageView togglePasswordImageView;
    private View vkLoginButton;
    private View yandexLoginButton;
    private boolean isPasswordVisible = false;
    private boolean isLoggingIn = false;
    private SharedPreferences settings;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        settings = getSharedPreferences("app_settings", MODE_PRIVATE);
        userRepository = new UserRepository();

        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        nextButton = findViewById(R.id.btnSubmit);
        registerTextView = findViewById(R.id.tvRegister);
        togglePasswordImageView = findViewById(R.id.ivTogglePassword);
        vkLoginButton = findViewById(R.id.btnVkLogin);
        yandexLoginButton = findViewById(R.id.btnYandexLogin);

        TextWatcher textWatcher = new SimpleTextWatcher(this::updateButtonState);

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled() && !isLoggingIn) {
                loginUser();
            }
        });

        registerTextView.setOnClickListener(v ->
                startActivity(new Intent(AuthActivity.this, RegistrationActivity.class)));

        vkLoginButton.setOnClickListener(v -> continueSocialAuth(PROVIDER_VK));
        yandexLoginButton.setOnClickListener(v -> continueSocialAuth(PROVIDER_YANDEX));
        togglePasswordImageView.setOnClickListener(v -> togglePasswordVisibility());

        String savedEmail = settings.getString(PREF_EMAIL, "");
        if (savedEmail != null && !savedEmail.isEmpty()) {
            emailEditText.setText(savedEmail);
            emailEditText.setSelection(savedEmail.length());
        }

        updateButtonState();
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateFields(email, password)) {
            updateButtonState();
            return;
        }

        isLoggingIn = true;
        nextButton.setEnabled(false);
        nextButton.setText("Вход...");

        userRepository.authUser(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(TokenResponse tokenResponse) {
                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_EMAIL, tokenResponse.record.email);
                    editor.putString(PREF_USER_ID, tokenResponse.record.id);
                    editor.putString(PREF_TOKEN, tokenResponse.token);
                    editor.putString(PREF_FIRST_NAME, tokenResponse.record.first_name);
                    editor.apply();

                    startActivity(new Intent(AuthActivity.this, PageAnaliseActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AuthActivity.this,
                            "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    isLoggingIn = false;
                    nextButton.setText("Далее");
                    updateButtonState();
                });
            }
        });
    }

    private void continueSocialAuth(String provider) {
        startActivity(PasswordActivity.createLoginIntent(this, provider));
    }

    private void updateButtonState() {
        if (!isLoggingIn) {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            boolean isValid = isValidEmail(email) && isValidPassword(password);
            nextButton.setEnabled(isValid);
        }

        nextButton.setBackgroundResource(R.drawable.button_enable);
        nextButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        nextButton.setAlpha(nextButton.isEnabled() ? 1f : 0.5f);
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8;
    }

    private boolean validateFields(String email, String password) {
        boolean isValid = true;

        if (!isValidEmail(email)) {
            emailEditText.setError("Введите корректный email");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }

        if (!isValidPassword(password)) {
            passwordEditText.setError("Минимум 8 символов");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        if (!isValid) {
            View invalidView = emailEditText.getError() != null ? emailEditText : passwordEditText;
            invalidView.requestFocus();
        }

        return isValid;
    }

    private void togglePasswordVisibility() {
        int selectionStart = passwordEditText.getSelectionStart();
        int selectionEnd = passwordEditText.getSelectionEnd();
        if (isPasswordVisible) {
            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePasswordImageView.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePasswordImageView.setImageResource(android.R.drawable.presence_invisible);
        }
        isPasswordVisible = !isPasswordVisible;
        passwordEditText.setSelection(selectionStart, selectionEnd);
    }

    private static final class SimpleTextWatcher implements TextWatcher {
        private final Runnable callback;

        private SimpleTextWatcher(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.run();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
