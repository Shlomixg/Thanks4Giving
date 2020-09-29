package com.tsk.thanks4giving;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    CircleImageView userImage;
    EditText userName;
    EditText userAge;
    EditText userAddress;
    Button saveBtn;
    String TAG = "Profile Frag";

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
        View rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
        userImage = rootView.findViewById(R.id.edit_profile_user_image);
        userName = rootView.findViewById(R.id.edit_profile_user_name_et);
        userAge = rootView.findViewById(R.id.edit_profile_user_age_et);
        userAddress = rootView.findViewById(R.id.edit_profile_user_address_et);

        saveBtn = rootView.findViewById(R.id.edit_profile_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: update firebase database with new information from EditTexts

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, new ProfileFragment(), TAG).commit();
            }
        });
        return rootView;
    }
}