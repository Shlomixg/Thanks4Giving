package com.tsk.thanks4giving;

import android.content.Context;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView userImage;
    TextView userName;
    TextView userEmail;
    TextView userAge;
    TextView userAddress;
    Button editBtn;
    String TAG = "Edit Profile Frag";

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
        userAge = rootView.findViewById(R.id.profile_user_age_tv);
        userAddress = rootView.findViewById(R.id.profile_user_address_tv);
        //TODO: get information from firebase database and insert into TextViews

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