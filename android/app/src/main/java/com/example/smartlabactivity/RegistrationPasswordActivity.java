package com.example.smartlabactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.UserRepository;
import com.example.smartlabactivity.api.dto.TokenResponse;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

public class RegistrationPasswordActivity extends AppCompatActivity {
    private static final String EXTRA_FIRST_NAME = "extra_first_name";
    private static final String EXTRA_LAST_NAME = "extra_last_name";
    private static final String EXTRA_PATRONYMIC = "extra_patronymic";
    private static final String EXTRA_BIRTHDAY = "extra_birthday";
    private static final String EXTRA_GENDER = "extra_gender";
    private static final String EXTRA_EMAIL = "extra_email";
    private static final String PREF_EMAIL = "Email";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_TOKEN = "auth_token";

    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private ImageView firstToggleImageView;
    private ImageView secondToggleImageView;
    private Button saveButton;
    private boolean isFirstPasswordVisible = false;
    private boolean isSecondPasswordVisible = false;
    private boolean isRegistering = false;

    private String firstName;
    private String lastName;
    private String patronymic;
    private String birthday;
    private String gender;
    private String email;

    private SharedPreferences settings;
    private UserRepository userRepository;

    public static Intent createIntent(
            AppCompatActivity activity,
            String firstName,
            String lastName,
            String patronymic,
            String birthday,
            String gender,
            String email
    ) {
        Intent intent = new Intent(activity, RegistrationPasswordActivity.class);
        intent.putExtra(EXTRA_FIRST_NAME, firstName);
        intent.putExtra(EXTRA_LAST_NAME, lastName);
        intent.putExtra(EXTRA_PATRONYMIC, patronymic);
        intent.putExtra(EXTRA_BIRTHDAY, birthday);
        intent.putExtra(EXTRA_GENDER, gender);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_password);

        settings = getSharedPreferences("app_settings", MODE_PRIVATE);
        userRepository = new UserRepository();

        firstName = getIntent().getStringExtra(EXTRA_FIRST_NAME);
        lastName = getIntent().getStringExtra(EXTRA_LAST_NAME);
        patronymic = getIntent().getStringExtra(EXTRA_PATRONYMIC);
        birthday = getIntent().getStringExtra(EXTRA_BIRTHDAY);
        gender = getIntent().getStringExtra(EXTRA_GENDER);
        email = getIntent().getStringExtra(EXTRA_EMAIL);

        passwordEditText = findViewById(R.id.etPassword);
        passwordConfirmEditText = findViewById(R.id.etPasswordConfirm);
        firstToggleImageView = findViewById(R.id.ivTogglePassword);
        secondToggleImageView = findViewById(R.id.ivTogglePasswordConfirm);
        saveButton = findViewById(R.id.btnSavePassword);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvPasswordTitle)).setText("Создание пароля");
        ((TextView) findViewById(R.id.tvPasswordSubtitle)).setText("Введите новый пароль");

        TextWatcher textWatcher = new SimpleTextWatcher(this::updateButtonState);
        passwordEditText.addTextChangedListener(textWatcher);
        passwordConfirmEditText.addTextChangedListener(textWatcher);

        firstToggleImageView.setOnClickListener(v -> togglePasswordVisibility(
                passwordEditText,
                firstToggleImageView,
                true
        ));
        secondToggleImageView.setOnClickListener(v -> togglePasswordVisibility(
                passwordConfirmEditText,
                secondToggleImageView,
                false
        ));
        saveButton.setOnClickListener(v -> registerUser());

        updateButtonState();
    }

    private void registerUser() {
        String password = passwordEditText.getText().toString().trim();
        String passwordConfirm = passwordConfirmEditText.getText().toString().trim();

        if (!validateFields(password, passwordConfirm)) {
            updateButtonState();
            return;
        }

        isRegistering = true;
        saveButton.setEnabled(false);
        saveButton.setText("Сохранение...");

        userRepository.registerUser(email, password, passwordConfirm,
                new UserRepository.RegisterCallback() {
                    @Override
                    public void onSuccess(UserRecordResponse user) {
                        loginAfterRegistration(password);
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> handleRegistrationError(error));
                    }
                });
    }

    private void loginAfterRegistration(String password) {
        userRepository.authUser(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(TokenResponse tokenResponse) {
                updateProfile(tokenResponse);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> handleRegistrationError(error));
            }
        });
    }

    private void updateProfile(TokenResponse tokenResponse) {
        userRepository.updateUser(
                tokenResponse.token,
                tokenResponse.record.id,
                firstName,
                lastName,
                patronymic,
                birthday,
                gender,
                new UserRepository.UpdateCallback() {
                    @Override
                    public void onSuccess(UserRecordResponse user) {
                        runOnUiThread(() -> {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(PREF_EMAIL, tokenResponse.record.email);
                            editor.putString(PREF_USER_ID, tokenResponse.record.id);
                            editor.putString(PREF_TOKEN, tokenResponse.token);
                            editor.apply();

                            Toast.makeText(RegistrationPasswordActivity.this,
                                    "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(
                                    RegistrationPasswordActivity.this,
                                    PageAnaliseActivity.class
                            ));
                            finishAffinity();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> handleRegistrationError(error));
                    }
                });
    }

    private void handleRegistrationError(String error) {
        Toast.makeText(this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
        isRegistering = false;
        saveButton.setText("Сохранить");
        updateButtonState();
    }

    private void updateButtonState() {
        if (!isRegistering) {
            saveButton.setEnabled(isValidPassword(passwordEditText.getText().toString().trim())
                    && passwordsMatch());
        }

        saveButton.setBackgroundResource(R.drawable.button_enable);
        saveButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        saveButton.setAlpha(saveButton.isEnabled() ? 1f : 0.5f);
    }

    private boolean validateFields(String password, String passwordConfirm) {
        boolean isValid = true;

        if (!isValidPassword(password)) {
            passwordEditText.setError("Минимум 8 символов");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        if (passwordConfirm.isEmpty() || !password.equals(passwordConfirm)) {
            passwordConfirmEditText.setError("Пароли не совпадают");
            isValid = false;
        } else {
            passwordConfirmEditText.setError(null);
        }

        return isValid;
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8;
    }

    private boolean passwordsMatch() {
        String password = passwordEditText.getText().toString().trim();
        String confirm = passwordConfirmEditText.getText().toString().trim();
        return !confirm.isEmpty() && password.equals(confirm);
    }

    private void togglePasswordVisibility(
            EditText editText,
            ImageView toggleView,
            boolean isFirstField
    ) {
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();
        boolean isVisible = isFirstField ? isFirstPasswordVisible : isSecondPasswordVisible;

        if (isVisible) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleView.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleView.setImageResource(android.R.drawable.presence_invisible);
        }

        if (isFirstField) {
            isFirstPasswordVisible = !isFirstPasswordVisible;
        } else {
            isSecondPasswordVisible = !isSecondPasswordVisible;
        }

        editText.setSelection(selectionStart, selectionEnd);
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
