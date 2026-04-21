package com.example.smartlabactivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {
    private static final String EXTRA_MODE = "password_mode";
    private static final String EXTRA_PROVIDER = "password_provider";
    private static final String MODE_LOGIN = "login";
    private static final int PIN_LENGTH = 4;

    private final StringBuilder currentPin = new StringBuilder();
    private ImageView[] pinDots;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private String provider;

    public static Intent createLoginIntent(AppCompatActivity activity, String provider) {
        Intent intent = new Intent(activity, PasswordActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_LOGIN);
        intent.putExtra(EXTRA_PROVIDER, provider);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        provider = getIntent().getStringExtra(EXTRA_PROVIDER);
        if (provider == null || provider.isEmpty()) {
            provider = MODE_LOGIN;
        }

        titleTextView = findViewById(R.id.tvPasswordTitle);
        subtitleTextView = findViewById(R.id.tvPasswordSubtitle);
        pinDots = new ImageView[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4)
        };

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
        configureScreen();
        setupKeypad();
        updateDots();
    }

    private void configureScreen() {
        titleTextView.setText("Вход");
        if ("vk".equals(provider)) {
            subtitleTextView.setText("Введите пароль для входа через VK");
        } else if ("yandex".equals(provider)) {
            subtitleTextView.setText("Введите пароль для входа через Yandex");
        } else {
            subtitleTextView.setText("Введите пароль для продолжения");
        }
    }

    private void setupKeypad() {
        int[] numberIds = {
                R.id.key1, R.id.key2, R.id.key3,
                R.id.key4, R.id.key5, R.id.key6,
                R.id.key7, R.id.key8, R.id.key9,
                R.id.key0
        };
        String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

        for (int i = 0; i < numberIds.length; i++) {
            final String value = values[i];
            findViewById(numberIds[i]).setOnClickListener(v -> appendDigit(value));
        }

        findViewById(R.id.keyDelete).setOnClickListener(v -> removeDigit());
    }

    private void appendDigit(String digit) {
        if (currentPin.length() >= PIN_LENGTH) {
            return;
        }

        currentPin.append(digit);
        updateDots();

        if (currentPin.length() == PIN_LENGTH) {
            handleCompletedPin();
        }
    }

    private void removeDigit() {
        int length = currentPin.length();
        if (length == 0) {
            return;
        }

        currentPin.deleteCharAt(length - 1);
        updateDots();
    }

    private void updateDots() {
        for (int i = 0; i < pinDots.length; i++) {
            pinDots[i].setImageResource(i < currentPin.length()
                    ? R.drawable.blue_circle
                    : R.drawable.blue_border_circle);
        }
    }

    private void handleCompletedPin() {
        startActivity(new Intent(this, PageAnaliseActivity.class));
        finish();
    }

    private void resetPin() {
        currentPin.setLength(0);
        updateDots();
    }
}
