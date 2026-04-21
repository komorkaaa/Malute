package com.example.smartlabactivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartlabactivity.api.ProjectRepository;
import com.example.smartlabactivity.api.dto.ProjectRecordResponse;

public class ProjectDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "extra_project_id";
    private static final String PREF_TOKEN = "auth_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        BottomNavHelper.setup(this, BottomNavHelper.Screen.PROJECTS);
        loadProject();
    }

    private void loadProject() {
        String projectId = getIntent().getStringExtra(EXTRA_ID);
        String token = getSharedPreferences("app_settings", MODE_PRIVATE)
                .getString(PREF_TOKEN, "");

        if (projectId == null || projectId.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Не удалось открыть проект", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new ProjectRepository().getProjectById(token, projectId, new ProjectRepository.ProjectCallback() {
            @Override
            public void onSuccess(ProjectRecordResponse project) {
                runOnUiThread(() -> bindProject(project));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ProjectDetailsActivity.this,
                            "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void bindProject(ProjectRecordResponse project) {
        ((TextView) findViewById(R.id.tvProjectTitle)).setText(safe(project.title));
        ((TextView) findViewById(R.id.tvProjectType)).setText(safe(project.type));
        ((TextView) findViewById(R.id.tvProjectStartDate)).setText(formatDate(project.date_start));
        ((TextView) findViewById(R.id.tvProjectEndDate)).setText(formatDate(project.date_end));
        ((TextView) findViewById(R.id.tvProjectAssignee)).setText(extractAssignee(project.description_source));
        ((TextView) findViewById(R.id.tvProjectSource)).setText(extractSource(project.description_source));
        ((TextView) findViewById(R.id.tvProjectCategory)).setText(safe(project.size));
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    private String formatDate(String value) {
        if (value == null || value.isEmpty()) {
            return "-";
        }
        if (value.length() == 10 && value.charAt(4) == '-') {
            return value.substring(8, 10) + "-" + value.substring(5, 7) + "-" + value.substring(0, 4);
        }
        return value;
    }

    private String extractAssignee(String description) {
        if (description == null || !description.contains("Кому: ")) {
            return "-";
        }
        String[] lines = description.split("\n");
        return lines.length > 0 ? lines[0].replace("Кому: ", "").trim() : "-";
    }

    private String extractSource(String description) {
        if (description == null || !description.contains("Источник: ")) {
            return "-";
        }
        String[] lines = description.split("\n");
        return lines.length > 1 ? lines[1].replace("Источник: ", "").trim() : "-";
    }
}
