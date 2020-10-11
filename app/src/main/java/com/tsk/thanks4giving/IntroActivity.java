package com.tsk.thanks4giving;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(getString(R.string.first_slide_title), getString(R.string.first_slide_desc),
                R.drawable.first_slide, Color.BLUE, Color.BLACK, Color.BLACK));
        addSlide(AppIntroFragment.newInstance(getString(R.string.second_slide_title), getString(R.string.second_slide_desc),
                R.drawable.second_slide, Color.CYAN, Color.BLACK, Color.BLACK));
        addSlide(AppIntroFragment.newInstance(getString(R.string.third_slide_title), getString(R.string.third_slide_desc),
                R.drawable.third_slide, Color.WHITE, Color.BLACK, Color.BLACK));
        addSlide(AppIntroFragment.newInstance(getString(R.string.fourth_slide_title), getString(R.string.fourth_slide_desc),
                R.drawable.fourth_slide, Color.CYAN, Color.BLACK, Color.BLACK));
        addSlide(AppIntroFragment.newInstance(getString(R.string.fifth_slide_title), getString(R.string.fifth_slide_desc),
                R.drawable.fifth_slide, Color.BLUE, Color.BLACK, Color.BLACK));
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.loadPrefs(sharedPrefs);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("firstRun", false).apply();
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.loadPrefs(sharedPrefs);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("firstRun", false).apply();
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
