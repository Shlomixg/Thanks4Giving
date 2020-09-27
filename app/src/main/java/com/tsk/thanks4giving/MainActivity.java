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

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    final String NEW_POST_FRAG = "New Post Fragment";
    final String RECYCLER_FRAG = "Recycler View Fragment";
    final String SIGNUP_FRAG = "signup fragment";
    final String LOGIN_FRAG = "login fragment";
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ArrayList<Post> postList = new ArrayList<>();
    static String displayName;
    static Uri photoURL;
    static String token;

    boolean isConnected = false;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = "android.resource://com.tsk.thanks4giving/drawable/ic_home";
        for (int i = 0; i < 10; i++) {
            Post post = new Post(path, path, i + 100, null, null);
            postList.add(post);
        }

        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);

        // TODO: make the fragments start below toolbar and go down to the bottom

        setSupportActionBar(toolbar);

        // TODO: Check if the user connected
        if (!isConnected) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.guest_main_menu);
        } else {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.main_menu);
            // TODO: Change the profile name & picture
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
                        break;
                    case R.id.nav_user_sign_up:
                        setFragment(new signupFragment(), SIGNUP_FRAG);
                        break;
                    case R.id.nav_user_login:
                        setFragment(new loginFragment(), LOGIN_FRAG);
                        break;
                    default:
                        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                }

                navigationView.setCheckedItem(item);
                setTitle(item.getTitle());
                drawerLayout.closeDrawers();
                return true;
            }
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setting the first fragment
        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
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
        // TODO: check if the user is connected
        if (isConnected) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
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
                // TODO: open profile in fragment
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
    public void onMessageEvent(MessageEvent event) {
        if(event.action.equals("signup"))
        {
            TextView userName = findViewById(R.id.nav_tv_user_name);
            userName.setText("Welcome " + event.name);
            Toast.makeText(this, event.action + event.name, Toast.LENGTH_SHORT).show();
        }
        else if(event.action.equals("login"))
        {
            TextView userName = findViewById(R.id.nav_tv_user_name);
            CircleImageView profilePic = findViewById(R.id.nav_profile_image);
            userName.setText("Welcome " + event.name);
            Glide.with(this).load(event.photoUrl).centerCrop().into(profilePic);
            Toast.makeText(this, event.action + event.name, Toast.LENGTH_SHORT).show();
        }
        isConnected = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}