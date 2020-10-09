package com.tsk.thanks4giving;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(getString(R.string.first_slide_title), getString(R.string.first_slide_desc), R.drawable.ic_account_box, Color.BLUE, Color.GRAY, Color.GRAY)); //TODO replace icon with app icon
        addSlide(AppIntroFragment.newInstance(getString(R.string.second_slide_title), getString(R.string.second_slide_desc), R.drawable.ic_account_box, Color.BLUE, Color.GRAY, Color.GRAY)); //TODO replace icon with signup from menu image
        addSlide(AppIntroFragment.newInstance(getString(R.string.third_slide_title), getString(R.string.third_slide_desc), R.drawable.ic_account_box, Color.BLUE, Color.GRAY, Color.GRAY)); //TODO replace icon with settings image highlight location switch
        addSlide(AppIntroFragment.newInstance(getString(R.string.fourth_slide_title), getString(R.string.fourth_slide_desc), R.drawable.ic_account_box, Color.BLUE, Color.GRAY, Color.GRAY)); //TODO replace icon with settings image highlight comments switch
        addSlide(AppIntroFragment.newInstance(getString(R.string.fifth_slide_title), getString(R.string.fifth_slide_desc), R.drawable.ic_account_box, Color.BLUE, Color.GRAY, Color.GRAY));
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
