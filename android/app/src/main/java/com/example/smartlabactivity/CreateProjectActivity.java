package com.example.smartlabactivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.ProjectRepository;
import com.example.smartlabactivity.api.dto.ProjectRecordResponse;

import java.util.Calendar;
import java.util.Locale;

public class CreateProjectActivity extends AppCompatActivity {
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_TOKEN = "auth_token";

    private AppCompatAutoCompleteTextView typeEditText;
    private EditText projectNameEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private AppCompatAutoCompleteTextView assigneeEditText;
    private EditText sourceEditText;
    private AppCompatAutoCompleteTextView categoryEditText;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        typeEditText = findViewById(R.id.etProjectType);
        projectNameEditText = findViewById(R.id.etProjectName);
        startDateEditText = findViewById(R.id.etProjectStartDate);
        endDateEditText = findViewById(R.id.etProjectEndDate);
        assigneeEditText = findViewById(R.id.etProjectAssignee);
        sourceEditText = findViewById(R.id.etProjectSource);
        categoryEditText = findViewById(R.id.etProjectCategory);
        confirmButton = findViewById(R.id.btnConfirmProject);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        setupDropdown(typeEditText, new String[]{"Дизайн", "Архитектура", "Интерьер"});
        setupDropdown(assigneeEditText, new String[]{"Себе", "Клиенту", "Команде"});
        setupDropdown(categoryEditText, new String[]{"Жилой", "Коммерческий", "Другое"});

        startDateEditText.setFocusable(false);
        endDateEditText.setFocusable(false);
        startDateEditText.setOnClickListener(v -> showDatePicker(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePicker(endDateEditText));

        TextWatcher textWatcher = new SimpleTextWatcher(this::updateButtonState);
        typeEditText.addTextChangedListener(textWatcher);
        projectNameEditText.addTextChangedListener(textWatcher);
        startDateEditText.addTextChangedListener(textWatcher);
        endDateEditText.addTextChangedListener(textWatcher);
        assigneeEditText.addTextChangedListener(textWatcher);
        sourceEditText.addTextChangedListener(textWatcher);
        categoryEditText.addTextChangedListener(textWatcher);

        confirmButton.setOnClickListener(v -> createProject());

        BottomNavHelper.setup(this, BottomNavHelper.Screen.PROJECTS);
        updateButtonState();
    }

    private void setupDropdown(AppCompatAutoCompleteTextView view, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );
        view.setAdapter(adapter);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                view.showDropDown();
            }
        });
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> target.setText(
                        String.format(Locale.getDefault(), "%02d-%02d-%04d",
                                dayOfMonth, month + 1, year)
                ),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateButtonState() {
        boolean isValid = hasText(typeEditText)
                && hasText(projectNameEditText)
                && hasText(startDateEditText)
                && hasText(endDateEditText)
                && hasText(assigneeEditText)
                && hasText(sourceEditText)
                && hasText(categoryEditText);

        confirmButton.setEnabled(isValid);
        confirmButton.setBackgroundResource(R.drawable.button_enable);
        confirmButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        confirmButton.setAlpha(isValid ? 1f : 0.5f);
    }

    private void createProject() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        String userId = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_USER_ID, "");

        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        confirmButton.setEnabled(false);
        confirmButton.setText("Сохранение...");

        new ProjectRepository().createProject(
                token,
                userId,
                projectNameEditText.getText().toString().trim(),
                typeEditText.getText().toString().trim(),
                toApiDate(startDateEditText.getText().toString().trim()),
                toApiDate(endDateEditText.getText().toString().trim()),
                assigneeEditText.getText().toString().trim(),
                sourceEditText.getText().toString().trim(),
                categoryEditText.getText().toString().trim(),
                new ProjectRepository.ProjectCallback() {
                    @Override
                    public void onSuccess(ProjectRecordResponse project) {
                        runOnUiThread(() -> {
                            Toast.makeText(CreateProjectActivity.this,
                                    "Проект создан", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(CreateProjectActivity.this,
                                    "Ошибка: " + error, Toast.LENGTH_LONG).show();
                            confirmButton.setText("Подтвердить");
                            updateButtonState();
                        });
                    }
                }
        );
    }

    private boolean hasText(EditText editText) {
        return !editText.getText().toString().trim().isEmpty();
    }

    private String toApiDate(String value) {
        if (value.length() == 10 && value.charAt(2) == '-') {
            return value.substring(6, 10) + "-" + value.substring(3, 5) + "-" + value.substring(0, 2);
        }
        return value;
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
