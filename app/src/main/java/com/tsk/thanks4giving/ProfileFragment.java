package com.tsk.thanks4giving;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    String TAG = "Edit Profile Frag";
    CircleImageView userImage;
    TextView userName;
    TextView userEmail;
    TextView userGender;
    TextView userAddress;
    Button editBtn;
    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        userImage = rootView.findViewById(R.id.profile_user_image);
        userName = rootView.findViewById(R.id.profile_user_name_tv);
        userEmail = rootView.findViewById(R.id.profile_user_email_tv);
        userGender = rootView.findViewById(R.id.profile_user_gender_tv);
        userAddress = rootView.findViewById(R.id.profile_user_address_tv);


        //get information from firebase database and insert into TextViews
        if(fbUser.getPhotoUrl() != null)
            Glide.with(getActivity()).load(fbUser.getPhotoUrl()).centerCrop().into(userImage);
        else{
            String path = "android.resource://com.tsk.thanks4giving/drawable/profile_man";
            Glide.with(getActivity()).load(Uri.parse(path)).centerCrop().into(userImage);
        }
        userName.setText(fbUser.getDisplayName());
        userEmail.setText(fbUser.getEmail());

        ref = mDatabase.child("users").child(fbUser.getUid()).child("gender");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String gender = snapshot.getValue(String.class);
                userGender.setText(gender);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        ref = mDatabase.child("users").child(fbUser.getUid()).child("address");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Address = snapshot.getValue(String.class);
                userAddress.setText(Address);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        editBtn = rootView.findViewById(R.id.edit_profile_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent,new EditProfileFragment() ,TAG).commit();
            }
        });
        return rootView;
    }


}