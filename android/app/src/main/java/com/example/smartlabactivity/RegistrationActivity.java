package com.example.smartlabactivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {
    private EditText firstNameEditText;
    private EditText patronymicEditText;
    private EditText lastNameEditText;
    private EditText birthdayEditText;
    private AppCompatAutoCompleteTextView genderEditText;
    private EditText emailEditText;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firstNameEditText = findViewById(R.id.etFirstName);
        patronymicEditText = findViewById(R.id.etPatronymic);
        lastNameEditText = findViewById(R.id.etLastName);
        birthdayEditText = findViewById(R.id.etBirthday);
        genderEditText = findViewById(R.id.etGender);
        emailEditText = findViewById(R.id.etEmail);
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

        nextButton.setOnClickListener(v -> submitProfile());
        updateButtonState();
    }

    private void submitProfile() {
        if (!validateFields()) {
            updateButtonState();
            return;
        }

        startActivity(RegistrationPasswordActivity.createIntent(
                this,
                firstNameEditText.getText().toString().trim(),
                lastNameEditText.getText().toString().trim(),
                patronymicEditText.getText().toString().trim(),
                birthdayEditText.getText().toString().trim(),
                mapGender(genderEditText.getText().toString().trim()),
                emailEditText.getText().toString().trim()
        ));
    }

    private void updateButtonState() {
        boolean isValid =
                hasText(firstNameEditText) &&
                hasText(patronymicEditText) &&
                hasText(lastNameEditText) &&
                hasText(birthdayEditText) &&
                hasText(genderEditText) &&
                isValidEmail(emailEditText.getText().toString().trim());

        nextButton.setEnabled(isValid);

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
