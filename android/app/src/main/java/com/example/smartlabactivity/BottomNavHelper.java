package com.example.smartlabactivity;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

final class BottomNavHelper {

    enum Screen {
        HOME,
        CATALOG,
        PROJECTS,
        PROFILE
    }

    private BottomNavHelper() {}

    static void setup(AppCompatActivity activity, Screen currentScreen) {
        LinearLayout navHome = activity.findViewById(R.id.navHome);
        LinearLayout navCatalog = activity.findViewById(R.id.navCatalog);
        LinearLayout navProjects = activity.findViewById(R.id.navProjects);
        LinearLayout navProfile = activity.findViewById(R.id.navProfile);

        ImageView iconHome = activity.findViewById(R.id.iconHome);
        ImageView iconCatalog = activity.findViewById(R.id.iconCatalog);
        ImageView iconProjects = activity.findViewById(R.id.iconProjects);
        ImageView iconProfile = activity.findViewById(R.id.iconProfile);

        TextView textHome = activity.findViewById(R.id.textHome);
        TextView textCatalog = activity.findViewById(R.id.textCatalog);
        TextView textProjects = activity.findViewById(R.id.textProjects);
        TextView textProfile = activity.findViewById(R.id.textProfile);

        int activeColor = ContextCompat.getColor(activity, R.color.blue_main);
        int inactiveColor = 0xFFB7C2D3;

        setItemState(iconHome, textHome, currentScreen == Screen.HOME, activeColor, inactiveColor);
        setItemState(iconCatalog, textCatalog, currentScreen == Screen.CATALOG, activeColor, inactiveColor);
        setItemState(iconProjects, textProjects, currentScreen == Screen.PROJECTS, activeColor, inactiveColor);
        setItemState(iconProfile, textProfile, currentScreen == Screen.PROFILE, activeColor, inactiveColor);

        navHome.setOnClickListener(v -> open(activity, currentScreen, Screen.HOME));
        navCatalog.setOnClickListener(v -> open(activity, currentScreen, Screen.CATALOG));
        navProjects.setOnClickListener(v -> open(activity, currentScreen, Screen.PROJECTS));
        navProfile.setOnClickListener(v -> open(activity, currentScreen, Screen.PROFILE));
    }

    private static void setItemState(
            ImageView icon,
            TextView text,
            boolean isActive,
            int activeColor,
            int inactiveColor
    ) {
        int color = isActive ? activeColor : inactiveColor;
        icon.setColorFilter(color);
        text.setTextColor(color);
    }

    private static void open(AppCompatActivity activity, Screen currentScreen, Screen targetScreen) {
        if (currentScreen == targetScreen) {
            return;
        }

        Intent intent;
        if (targetScreen == Screen.HOME) {
            intent = new Intent(activity, PageAnaliseActivity.class);
        } else if (targetScreen == Screen.CATALOG) {
            intent = new Intent(activity, CatalogActivity.class);
        } else if (targetScreen == Screen.PROJECTS) {
            intent = new Intent(activity, ProjectsActivity.class);
        } else {
            intent = new Intent(activity, UserCardActivity.class);
        }

        activity.startActivity(intent);
        activity.finish();
    }
}
