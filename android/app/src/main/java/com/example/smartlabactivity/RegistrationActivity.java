package com.example.smartlabactivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.UserRepository;
import com.example.smartlabactivity.api.dto.TokenResponse;

import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {
    private static final String PREF_EMAIL = "Email";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_TOKEN = "auth_token";

    private EditText firstNameEditText;
    private EditText patronymicEditText;
    private EditText lastNameEditText;
    private EditText birthdayEditText;
    private AppCompatAutoCompleteTextView genderEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private Button nextButton;
    private SharedPreferences settings;
    private UserRepository userRepository;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        settings = getSharedPreferences("app_settings", MODE_PRIVATE);
        userRepository = new UserRepository();

        firstNameEditText = findViewById(R.id.etFirstName);
        patronymicEditText = findViewById(R.id.etPatronymic);
        lastNameEditText = findViewById(R.id.etLastName);
        birthdayEditText = findViewById(R.id.etBirthday);
        genderEditText = findViewById(R.id.etGender);
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        passwordConfirmEditText = findViewById(R.id.etPasswordConfirm);
        nextButton = findViewById(R.id.btnSubmit);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{"Мужской", "Женский", "Другой"}
        );
        genderEditText.setAdapter(genderAdapter);
        genderEditText.setOnClickListener(v -> genderEditText.showDropDown());
        genderEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                genderEditText.showDropDown();
            }
        });
        birthdayEditText.setFocusable(false);
        birthdayEditText.setClickable(true);
        birthdayEditText.setOnClickListener(v -> showDatePicker());

        TextWatcher textWatcher = new SimpleTextWatcher(this::updateButtonState);
        firstNameEditText.addTextChangedListener(textWatcher);
        patronymicEditText.addTextChangedListener(textWatcher);
        lastNameEditText.addTextChangedListener(textWatcher);
        birthdayEditText.addTextChangedListener(textWatcher);
        genderEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        passwordConfirmEditText.addTextChangedListener(textWatcher);

        nextButton.setOnClickListener(v -> submitProfile());
        updateButtonState();
    }

    private void submitProfile() {
        if (!validateFields()) {
            updateButtonState();
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordConfirm = passwordConfirmEditText.getText().toString().trim();

        isRegistering = true;
        nextButton.setEnabled(false);
        nextButton.setText("Регистрация...");

        userRepository.registerUser(email, password, passwordConfirm,
                new UserRepository.RegisterCallback() {
                    @Override
                    public void onSuccess(com.example.smartlabactivity.api.dto.UserRecordResponse user) {
                        loginAfterRegistration(email, password);
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> handleRegistrationError(error));
                    }
                });
    }

    private void loginAfterRegistration(String email, String password) {
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
                firstNameEditText.getText().toString().trim(),
                lastNameEditText.getText().toString().trim(),
                patronymicEditText.getText().toString().trim(),
                birthdayEditText.getText().toString().trim(),
                mapGender(genderEditText.getText().toString().trim()),
                new UserRepository.UpdateCallback() {
                    @Override
                    public void onSuccess(com.example.smartlabactivity.api.dto.UserRecordResponse user) {
                        runOnUiThread(() -> {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(PREF_EMAIL, tokenResponse.record.email);
                            editor.putString(PREF_USER_ID, tokenResponse.record.id);
                            editor.putString(PREF_TOKEN, tokenResponse.token);
                            editor.apply();

                            Toast.makeText(RegistrationActivity.this,
                                    "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistrationActivity.this, PageAnaliseActivity.class));
                            finish();
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
        nextButton.setText("Далее");
        updateButtonState();
    }

    private void updateButtonState() {
        if (!isRegistering) {
            boolean isValid =
                    hasText(firstNameEditText) &&
                    hasText(patronymicEditText) &&
                    hasText(lastNameEditText) &&
                    hasText(birthdayEditText) &&
                    hasText(genderEditText) &&
                    isValidEmail(emailEditText.getText().toString().trim()) &&
                    isValidPassword(passwordEditText.getText().toString().trim()) &&
                    passwordsMatch();

            nextButton.setEnabled(isValid);
        }

        nextButton.setBackgroundResource(R.drawable.button_enable);
        nextButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        nextButton.setAlpha(nextButton.isEnabled() ? 1f : 0.5f);
    }

    private boolean validateFields() {
        boolean isValid = true;
        View firstInvalidView = null;

        if (!hasText(firstNameEditText)) {
            firstNameEditText.setError("Введите имя");
            firstInvalidView = firstInvalidView == null ? firstNameEditText : firstInvalidView;
            isValid = false;
        } else {
            firstNameEditText.setError(null);
        }

        if (!hasText(patronymicEditText)) {
            patronymicEditText.setError("Введите отчество");
            firstInvalidView = firstInvalidView == null ? patronymicEditText : firstInvalidView;
            isValid = false;
        } else {
            patronymicEditText.setError(null);
        }

        if (!hasText(lastNameEditText)) {
            lastNameEditText.setError("Введите фамилию");
            firstInvalidView = firstInvalidView == null ? lastNameEditText : firstInvalidView;
            isValid = false;
        } else {
            lastNameEditText.setError(null);
        }

        if (!hasText(birthdayEditText)) {
            birthdayEditText.setError("Введите дату рождения");
            firstInvalidView = firstInvalidView == null ? birthdayEditText : firstInvalidView;
            isValid = false;
        } else {
            birthdayEditText.setError(null);
        }

        if (!hasText(genderEditText)) {
            genderEditText.setError("Выберите пол");
            firstInvalidView = firstInvalidView == null ? genderEditText : firstInvalidView;
            isValid = false;
        } else {
            genderEditText.setError(null);
        }

        String email = emailEditText.getText().toString().trim();
        if (!isValidEmail(email)) {
            emailEditText.setError("Введите корректную почту");
            firstInvalidView = firstInvalidView == null ? emailEditText : firstInvalidView;
            isValid = false;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString().trim();
        if (!isValidPassword(password)) {
            passwordEditText.setError("Минимум 8 символов");
            firstInvalidView = firstInvalidView == null ? passwordEditText : firstInvalidView;
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        if (!passwordsMatch()) {
            passwordConfirmEditText.setError("Пароли не совпадают");
            firstInvalidView = firstInvalidView == null ? passwordConfirmEditText : firstInvalidView;
            isValid = false;
        } else {
            passwordConfirmEditText.setError(null);
        }

        if (firstInvalidView != null) {
            firstInvalidView.requestFocus();
        }

        return isValid;
    }

    private boolean hasText(EditText editText) {
        return !editText.getText().toString().trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8;
    }

    private boolean passwordsMatch() {
        String password = passwordEditText.getText().toString().trim();
        String confirm = passwordConfirmEditText.getText().toString().trim();
        return !confirm.isEmpty() && password.equals(confirm);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> birthdayEditText.setText(
                        String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                year, month + 1, dayOfMonth)
                ),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private String mapGender(String value) {
        if ("Мужской".equals(value)) {
            return "male";
        }
        if ("Женский".equals(value)) {
            return "female";
        }
        return "other";
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
