package com.example.smartlabactivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ProjectsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        BottomNavHelper.setup(this, BottomNavHelper.Screen.PROJECTS);
    }
}
