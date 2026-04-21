package com.example.smartlabactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartlabactivity.api.ProjectRepository;
import com.example.smartlabactivity.api.dto.ProjectRecordResponse;
import com.example.smartlabactivity.api.dto.ProjectsListResponse;

public class ProjectsActivity extends AppCompatActivity {
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_TOKEN = "auth_token";

    private LinearLayout projectsContainer;
    private TextView emptyProjectsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        projectsContainer = findViewById(R.id.projectsContainer);
        emptyProjectsTextView = findViewById(R.id.tvEmptyProjects);

        TextView addProjectTextView = findViewById(R.id.tvAddProject);
        addProjectTextView.setOnClickListener(v ->
                startActivity(new Intent(this, CreateProjectActivity.class)));

        BottomNavHelper.setup(this, BottomNavHelper.Screen.PROJECTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        String userId = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_USER_ID, "");

        if (token.isEmpty()) {
            emptyProjectsTextView.setVisibility(View.VISIBLE);
            emptyProjectsTextView.setText("Сначала войдите в аккаунт");
            projectsContainer.removeAllViews();
            return;
        }

        new ProjectRepository().getProjects(token, userId, new ProjectRepository.ProjectsCallback() {
            @Override
            public void onSuccess(ProjectsListResponse response) {
                runOnUiThread(() -> showProjects(response));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        ProjectsActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void showProjects(ProjectsListResponse response) {
        projectsContainer.removeAllViews();
        if (response.items == null || response.items.isEmpty()) {
            emptyProjectsTextView.setVisibility(View.VISIBLE);
            emptyProjectsTextView.setText("Пока нет проектов");
            return;
        }

        emptyProjectsTextView.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ProjectRecordResponse project : response.items) {
            View cardView = inflater.inflate(R.layout.item_project_card, projectsContainer, false);

            ((TextView) cardView.findViewById(R.id.tvProjectItemTitle)).setText(safe(project.title));
            ((TextView) cardView.findViewById(R.id.tvProjectItemTime)).setText(buildSubtitle(project));
            cardView.findViewById(R.id.btnOpenProject).setOnClickListener(v -> openProject(project.id));

            projectsContainer.addView(cardView);
        }
    }

    private void openProject(String projectId) {
        Intent intent = new Intent(this, ProjectDetailsActivity.class);
        intent.putExtra(ProjectDetailsActivity.EXTRA_ID, projectId);
        startActivity(intent);
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "Без названия" : value;
    }

    private String buildSubtitle(ProjectRecordResponse project) {
        if (project.created != null && project.created.length() >= 10) {
            return "Создан " + project.created.substring(0, 10);
        }
        return "Открыть проект";
    }
}
