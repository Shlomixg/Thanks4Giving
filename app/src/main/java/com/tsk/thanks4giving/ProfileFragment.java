package com.tsk.thanks4giving;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
    CircleImageView userImage;
    TextView userName;
    TextView userEmail;
    TextView userGender;
    TextView userAddress;
    Button editBtn;
    Button msgBtn;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    LovelyProgressDialog progressDialog;

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

        progressDialog = new LovelyProgressDialog(getContext()).setMessage("Wait").setCancelable(false).setTitle("Wait");
        progressDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mUserUid = getArguments().getString(ARG_USER_UID);
        }


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        userImage = rootView.findViewById(R.id.profile_user_image);
        userName = rootView.findViewById(R.id.profile_user_name_tv);
        userEmail = rootView.findViewById(R.id.profile_user_email_tv);
        userGender = rootView.findViewById(R.id.profile_user_gender_tv);
        userAddress = rootView.findViewById(R.id.profile_user_address_tv);
        msgBtn = rootView.findViewById(R.id.message_btn);
        editBtn = rootView.findViewById(R.id.edit_profile_btn);

        ref = mDatabase.child("users").child(mUserUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userName.setText(user.name);
                    userEmail.setText(user.email);
                    if (user.profilePhoto != null) {
                        Glide.with(getActivity()).load(user.profilePhoto).centerCrop().into(userImage);
                    } else {
                        String path = "android.resource://com.tsk.thanks4giving/drawable/profile_man";
                        Glide.with(getActivity()).load(Uri.parse(path)).centerCrop().into(userImage);
                    }
                    userGender.setText(user.gender);
                    userAddress.setText(user.address);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (mUserUid.equals(currentUser.getUid())) {
            msgBtn.setVisibility(View.GONE);
        } else {
            editBtn.setVisibility(View.GONE);
            userEmail.setVisibility(View.GONE);
        }
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, new EditProfileFragment(), TAG).addToBackStack(null).commit();
            }
        });
        return rootView;
    }
}