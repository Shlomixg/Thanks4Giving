package com.tsk.thanks4giving;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Map;

public class Utils {

    static SharedPreferences sharedPrefs;
    private final static int THEME_DEFAULT = 1;
    private final static int THEME_LIGHT = 2;
    private final static int THEME_DARK = 3;
    private static final String KEY_PREF_THEME = "pref_theme";
    private static final String KEY_COMMENT = "commentSwitch";

    static void loadPrefs(SharedPreferences sharedPrefs) {
        Utils.sharedPrefs = sharedPrefs;
        Map<String, ?> allEntries = sharedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            findPref(sharedPrefs, entry.getKey());
        }
    }

    static void findPref(SharedPreferences sharedPrefs, String key) {
        Utils.sharedPrefs = sharedPrefs;
        switch (key) {
            case KEY_PREF_THEME:
                String theme = sharedPrefs.getString(key, "1");
                Utils.setTheme(Integer.parseInt(theme));
                break;
            case KEY_COMMENT:
                MainActivity.setCommentSwitch(sharedPrefs.getBoolean(KEY_COMMENT, true));
                break;
        }
    }

    private static void setTheme(int theme) {
        switch (theme) {
            case THEME_DEFAULT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }
}