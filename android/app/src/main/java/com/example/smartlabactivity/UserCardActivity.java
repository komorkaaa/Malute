package com.example.smartlabactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.smartlabactivity.api.OrderRepository;
import com.example.smartlabactivity.api.UserRepository;
import com.example.smartlabactivity.api.dto.OrdersListResponse;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

public class UserCardActivity extends AppCompatActivity {
    private static final String PREF_EMAIL = "Email";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_TOKEN = "auth_token";
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

        String email = settings.getString(PREF_EMAIL, "");
        profileNameTextView.setText("Профиль");
        profileEmailTextView.setText(email == null || email.isEmpty() ? "email@example.com" : email);

        notificationsSwitch.setChecked(settings.getBoolean(PREF_NOTIFICATIONS, true));
        notificationsSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                settings.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
        );

        ordersRow.setOnClickListener(v -> loadOrdersCount());

        BottomNavHelper.setup(this, BottomNavHelper.Screen.PROFILE);
        loadProfile(profileNameTextView, profileEmailTextView);

        privacyTextView.setOnClickListener(v -> Toast.makeText(this, "Политика конфиденциальности", Toast.LENGTH_SHORT).show());
        agreementTextView.setOnClickListener(v -> Toast.makeText(this, "Пользовательское соглашение", Toast.LENGTH_SHORT).show());
        logoutTextView.setOnClickListener(v -> {
            settings.edit().clear().apply();
            Intent intent = new Intent(UserCardActivity.this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadProfile(TextView nameTextView, TextView emailTextView) {
        String token = settings.getString(PREF_TOKEN, "");
        String userId = settings.getString(PREF_USER_ID, "");
        if (token == null || token.isEmpty() || userId == null || userId.isEmpty()) {
            return;
        }

        new UserRepository().getUserById(token, userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(UserRecordResponse user) {
                runOnUiThread(() -> {
                    nameTextView.setText(user.first_name == null || user.first_name.isEmpty()
                            ? "Профиль" : user.first_name);
                    emailTextView.setText(user.email == null || user.email.isEmpty()
                            ? "email@example.com" : user.email);
                    settings.edit().putString(PREF_EMAIL, user.email).apply();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        UserCardActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_SHORT
                ).show());
            }
        });
    }

    private void loadOrdersCount() {
        String token = settings.getString(PREF_TOKEN, "");
        if (token == null || token.isEmpty()) {
            return;
        }

        new OrderRepository().getOrders(token, new OrderRepository.OrdersCallback() {
            @Override
            public void onSuccess(OrdersListResponse response) {
                runOnUiThread(() -> Toast.makeText(
                        UserCardActivity.this,
                        "Заказов: " + response.totalItems,
                        Toast.LENGTH_SHORT
                ).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        UserCardActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_SHORT
                ).show());
            }
        });
    }
}
