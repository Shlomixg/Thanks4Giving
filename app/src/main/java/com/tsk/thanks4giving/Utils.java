package com.tsk.thanks4giving;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class Utils {

    static SharedPreferences sharedPrefs;

    private final static int THEME_DEFAULT = 1;
    private final static int THEME_LIGHT = 2;
    private final static int THEME_DARK = 3;

    private static final String KEY_PREF_THEME = "pref_theme";

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
        }
    }

    // TODO: Delete this function or not?
    public static int getTheme() {
        String theme = sharedPrefs.getString(KEY_PREF_THEME, "1");
        return Integer.parseInt(theme);
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
