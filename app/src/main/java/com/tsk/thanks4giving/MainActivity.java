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
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
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
    ArrayList<Post> postList = new ArrayList<>();

    TextView user_name_tv;
    CircleImageView profile_pic_iv;

    FirebaseUser currentFBUser;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener authStateListener;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference users = db.getReference("users");
    DatabaseReference posts = db. getReference("posts");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.loadPrefs(sharedPrefs);

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
                FirebaseUser fbUser = mAuth.getCurrentUser();
                navigationView.getMenu().clear();
                if ( fbUser != null) { // Sign up or login
                    currentFBUser = fbUser;
                    navigationView.inflateMenu(R.menu.main_menu);
                    invalidateOptionsMenu();

                    user_name_tv.setText(fbUser.getDisplayName());
                    Glide.with(getApplicationContext()).load(R.drawable.profile_woman).centerCrop().into(profile_pic_iv);
                } else {
                    currentFBUser = null;
                    navigationView.inflateMenu(R.menu.guest_main_menu);
                    invalidateOptionsMenu();

                    user_name_tv.setText(getString(R.string.guest));
                    Glide.with(getApplicationContext()).load(R.drawable.profile_man).centerCrop().into(profile_pic_iv);
                }
            }
        };

        // Setting the first fragment
        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        mAuth.addAuthStateListener(authStateListener);
        currentFBUser = mAuth.getCurrentUser();
    }

    private void setFragment(Fragment fragment, String FRAG) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flContent, fragment, FRAG);
        if (!FRAG.equals(RECYCLER_FRAG)) transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentFBUser != null) {
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
                String currUserUid = currentFBUser.getUid();
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageUserEvent(MessageUserEvent event) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mAuth.removeAuthStateListener(authStateListener);
    }
}