package com.tsk.thanks4giving;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    final String NEW_POST_FRAG = "New Post Fragment";
    final String RECYCLER_FRAG = "Recycler View Fragment";
    final String SIGNUP_FRAG = "Signup Fragment";
    final String LOGIN_FRAG = "Login Fragment";
    final String PROFILE_FRAG = "Profile Fragment";
    final String SETTINGS_FRAG = "Settings Fragment";

    SharedPreferences sharedPrefs;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    TextView user_name_tv, midTV;
    ImageView midIV;
    LinearLayout midLO;
    CircleImageView profile_pic_iv;
    public static boolean commentSwitch = true;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseMessaging messaging = FirebaseMessaging.getInstance();
    FirebaseUser currentUser;
    FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.loadPrefs(sharedPrefs);

        if (sharedPrefs.getBoolean("firstRun", true)) {
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
        }

        scheduleJob();
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);

        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
                        navigationView.setCheckedItem(item);
                        setTitle(item.getTitle());
                        break;
                    case R.id.nav_user_sign_up:
                        setFragment(new SignupFragment(), SIGNUP_FRAG);
                        break;
                    case R.id.nav_user_login:
                        setFragment(new LoginFragment(), LOGIN_FRAG);
                        break;
                    case R.id.nav_user_logout:
                        mAuth.signOut();
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.logged_out), Snackbar.LENGTH_SHORT).show();
                        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
                        break;
                    case R.id.nav_settings:
                        setFragment(new SettingsFragment(), SETTINGS_FRAG);
                        break;
                    case R.id.nav_my_profile:
                        String currUserUid = currentUser.getUid();
                        Bundle bundle = new Bundle();
                        bundle.putString("userUid", currUserUid);
                        ProfileFragment fragment = new ProfileFragment();
                        fragment.setArguments(bundle);
                        setFragment(fragment, PROFILE_FRAG);
                        break;
                    default:
                        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
                        navigationView.setCheckedItem(item);
                        setTitle(item.getTitle());
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View header = navigationView.getHeaderView(0);
        user_name_tv = header.findViewById(R.id.nav_tv_user_name);
        profile_pic_iv = header.findViewById(R.id.nav_profile_image);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = mAuth.getCurrentUser();
                navigationView.getMenu().clear();
                if (currentUser != null) { // Sign up or login
                    if (commentSwitch) {
                        String topic = "commentNotif" + mAuth.getUid();
                        messaging.subscribeToTopic(topic);
                        Log.d("fcm", "authState: " + topic + " subscribed");
                    }
                    navigationView.inflateMenu(R.menu.main_menu);
                    invalidateOptionsMenu();

                    user_name_tv.setText(currentUser.getDisplayName());
                    Glide.with(MainActivity.this).load(currentUser.getPhotoUrl()).centerCrop().into(profile_pic_iv);
                } else { // Logout or guest user
                    navigationView.inflateMenu(R.menu.guest_main_menu);
                    invalidateOptionsMenu();

                    user_name_tv.setText(getString(R.string.guest));
                    Glide.with(getApplicationContext()).load(R.drawable.profile_man).centerCrop().into(profile_pic_iv);
                }
            }
        };

        Intent intent = this.getIntent();
        // Setting the first fragment
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetwork() == null) {
            midLO = findViewById(R.id.textView_area);
            midIV = findViewById(R.id.mid_image);
            midTV = findViewById(R.id.mid_text);
            midLO.setVisibility(View.VISIBLE);
            midIV.setImageResource(R.drawable.ic_baseline_cloud_off_24);
            midTV.setText(getString(R.string.no_internet));
        } else if (intent.hasExtra("post")) {
            String postID = intent.getStringExtra("post");
            FragmentManager fragmentManager = getSupportFragmentManager();
            Bundle bundle = new Bundle();
            bundle.putString("PostId", postID);
            PostFragment postFragment = new PostFragment();
            postFragment.setArguments(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.flContent, postFragment, "POST_FRAG").commit();
        } else {
            setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
        EventBus.getDefault().register(this);
        currentUser = mAuth.getCurrentUser();
    }

    private void setFragment(Fragment fragment, String FRAG) {
        // Prevent opening the same fragment twice
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> list = fragmentManager.getFragments();
        // Get last fragment
        if (!list.isEmpty()) {
            Fragment topFragment = list.get(list.size() - 1);
            if (topFragment != null && topFragment.getTag().equals(FRAG)) {
                fragmentManager.popBackStackImmediate();
            }
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flContent, fragment, FRAG);
        if (!FRAG.equals(RECYCLER_FRAG)) transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentUser != null) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        } else {
            menu.clear();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.new_post:
                setFragment(new NewPostFragment(), NEW_POST_FRAG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mAuth.removeAuthStateListener(authStateListener);
    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, GoogleConnectionRefreshService.class);
        JobInfo jobInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(123, componentName)
                    .setPeriodic(60 * 1000, 60 * 1000)
                    .setPersisted(false)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(123, componentName)
                    .setPeriodic(60 * 1000)
                    .setPersisted(false)
                    .build();
        }
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("ddd", "Job scheduled");
        } else {
            Log.d("ddd", "Job scheduling failed");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d("ddd", "event reached fragment, message:" + event.msg);
        //Refresh connection with google servers
        this.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        this.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
    }

    public static void setCommentSwitch(boolean val) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        String topic = "commentNotif" + uid;
        if (val) {
            commentSwitch = true;
            messaging.subscribeToTopic(topic);
            Log.d("fcm", "Switch: " + topic + " subscribed");
        } else {
            commentSwitch = false;
            messaging.unsubscribeFromTopic(topic);
            Log.d("fcm", "Switch: " + topic + " unsubscribed");
        }
    }
}