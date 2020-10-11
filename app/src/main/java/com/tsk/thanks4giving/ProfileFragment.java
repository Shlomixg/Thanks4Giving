package com.tsk.thanks4giving;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    String TAG = "Edit Profile Frag";
    boolean isCurrentUser = false;

    CircleImageView profile_civ;
    TextView name_tv, email_tv, gender_tv, address_tv;
    FloatingActionButton fab;
    MaterialCardView active_items_card, delivered_items_card;
    LovelyProgressDialog progressDialog;
    TextView active_items_label, delivered_items_label;
    int delivered_items = 0;
    int available_items = 0;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    final DatabaseReference posts = mDatabase.child("posts");

    private static final String ARG_USER_UID = "userUid";
    private String mUserUid;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * @param userUid UID of the user to show
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(String userUid) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_UID, userUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setTitle(R.string.dialog_loading_title)
                .setMessage(R.string.dialog_loading_msg);
        progressDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mUserUid = getArguments().getString(ARG_USER_UID);
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profile_civ = rootView.findViewById(R.id.profile_user_image);
        active_items_label = rootView.findViewById(R.id.active_items_label);
        delivered_items_label = rootView.findViewById(R.id.delivered_items_label);
        name_tv = rootView.findViewById(R.id.profile_user_name_tv);
        email_tv = rootView.findViewById(R.id.profile_user_email_tv);
        gender_tv = rootView.findViewById(R.id.profile_user_gender_tv);
        address_tv = rootView.findViewById(R.id.profile_user_address_tv);
        active_items_card = rootView.findViewById(R.id.active_items_card);
        delivered_items_card = rootView.findViewById(R.id.delivered_items_card);
        fab = rootView.findViewById(R.id.profile_fab);

        posts.orderByChild("uid").equalTo(mUserUid);
        posts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                available_items = delivered_items = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post.getUserUid().equals(mUserUid) && post.getStatus() == 1) {
                        available_items++;
                    } else if (post.getUserUid().equals(mUserUid) && post.getStatus() == 0) {
                        delivered_items++;
                    }
                    active_items_label.setText("" + available_items);
                    delivered_items_label.setText("" + delivered_items);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (currentUser == null) {
            fab.setVisibility(View.GONE);
            email_tv.setVisibility(View.GONE);
            address_tv.setVisibility(View.GONE);
        } else if (mUserUid.equals(currentUser.getUid())) {
            isCurrentUser = true;
            fab.setImageResource(R.drawable.ic_edit);
        } else {
            email_tv.setVisibility(View.GONE);
            address_tv.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCurrentUser) {
                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.flContent, new EditProfileFragment(), TAG).addToBackStack(null).commit();
                } else {
                    // TODO: Send Message
                    Toast.makeText(getContext(), "TODO: Send Message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        active_items_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserPosts(mUserUid, 1);
            }
        });

        delivered_items_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserPosts(mUserUid, 0);
            }
        });

        final String[] GENDERS = getResources().getStringArray(R.array.genders);
        ref = mDatabase.child("users").child(mUserUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.profilePhoto != null) {
                        Glide.with(getContext()).load(user.profilePhoto).centerCrop().into(profile_civ);
                    }
                    name_tv.setText(user.name);
                    email_tv.setText(user.email);
                    address_tv.setText(user.address);
                    gender_tv.setText(GENDERS[user.gender]);

                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Profile Log", "--- Profile display failed");
                Log.d("Profile Log", "--- Error: " + error);
                progressDialog.dismiss();
            }
        });
        return rootView;
    }

    public void showUserPosts(String mUserUid, int status) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("userUid", mUserUid);
        bundle.putInt("itemsStatus", status);
        RecyclerViewFragment rvFragment = new RecyclerViewFragment();
        rvFragment.setArguments(bundle);
        transaction.replace(R.id.flProfileContent, rvFragment, "USER_POSTS_FRAG").commit();
    }
}