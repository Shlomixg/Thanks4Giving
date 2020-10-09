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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "job scheduler";
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
    TextView user_name_tv;
    CircleImageView profile_pic_iv;
    FirebaseUser fbUser;
    public static boolean commentSwitch = true;

    FirebaseUser currentUser;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMessaging messaging = FirebaseMessaging.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.loadPrefs(sharedPrefs);

        if(sharedPrefs.getBoolean("firstRun",true))
        {
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();
        }

        boolean autoLocation = sharedPrefs.getBoolean("locationPref", false);
        if (autoLocation)
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
                    Glide.with(MainActivity.this)
                            .load(currentUser.getPhotoUrl())
                            .centerCrop().into(profile_pic_iv);
                } else {
                    navigationView.inflateMenu(R.menu.guest_main_menu);
                    invalidateOptionsMenu();

                    user_name_tv.setText(getString(R.string.guest));
                    Glide.with(getApplicationContext())
                            .load(R.drawable.profile_man)
                            .centerCrop().into(profile_pic_iv);
                }
            }
        };
        // Setting the first fragment
        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
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
                fragmentManager.popBackStack();
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
            case R.id.my_profile:
                String currUserUid = currentUser.getUid();
                Bundle bundle = new Bundle();
                bundle.putString("userUid", currUserUid);
                ProfileFragment fragment = new ProfileFragment();
                fragment.setArguments(bundle);
                setFragment(fragment, PROFILE_FRAG);
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
        mAuth.removeAuthStateListener(authStateListener);
    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, LocationJobService.class);
        JobInfo jobInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(123, componentName)
                    .setPeriodic(5 * 60 * 1000, 5 * 60 * 1000)
                    .setPersisted(false)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(123, componentName)
                    .setPeriodic(5 * 60 * 1000)
                    .setPersisted(false)
                    .build();
        }
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS)
            Log.d("ddd", "Job scheduled");
        else Log.d("ddd", "Job scheduling failed");
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