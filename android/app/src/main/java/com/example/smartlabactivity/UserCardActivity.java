package com.example.smartlabactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class UserCardActivity extends AppCompatActivity {
    private static final String PREF_EMAIL = "Email";
    private static final String PREF_FIRST_NAME = "profile_first_name";
    private static final String PREF_NOTIFICATIONS = "profile_notifications_enabled";

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_card);

        settings = getSharedPreferences("app_settings", MODE_PRIVATE);

        TextView profileNameTextView = findViewById(R.id.tvProfileName);
        TextView profileEmailTextView = findViewById(R.id.tvProfileEmail);
        SwitchCompat notificationsSwitch = findViewById(R.id.switchNotifications);
        android.widget.LinearLayout ordersRow = findViewById(R.id.rowOrders);
        TextView privacyTextView = findViewById(R.id.tvPrivacy);
        TextView agreementTextView = findViewById(R.id.tvAgreement);
        TextView logoutTextView = findViewById(R.id.tvLogout);

        String firstName = settings.getString(PREF_FIRST_NAME, "");
        String email = settings.getString(PREF_EMAIL, "");

        profileNameTextView.setText(firstName == null || firstName.isEmpty() ? "Профиль" : firstName);
        profileEmailTextView.setText(email == null || email.isEmpty() ? "email@example.com" : email);

        notificationsSwitch.setChecked(settings.getBoolean(PREF_NOTIFICATIONS, true));
        notificationsSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                settings.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
        );

        BottomNavHelper.setup(this, BottomNavHelper.Screen.PROFILE);

        ordersRow.setOnClickListener(v -> Toast.makeText(this, "Мои заказы пока не готовы", Toast.LENGTH_SHORT).show());
        privacyTextView.setOnClickListener(v -> Toast.makeText(this, "Политика конфиденциальности", Toast.LENGTH_SHORT).show());
        agreementTextView.setOnClickListener(v -> Toast.makeText(this, "Пользовательское соглашение", Toast.LENGTH_SHORT).show());
        logoutTextView.setOnClickListener(v -> {
            settings.edit().clear().apply();
            Intent intent = new Intent(UserCardActivity.this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
