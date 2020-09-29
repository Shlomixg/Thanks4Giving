package com.tsk.thanks4giving;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

public class SignupFragment extends Fragment {

    EditText email;
    EditText fullname;
    EditText password;
    Button confirmBtn;
    FirebaseAuth mAuth;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);
        email = rootView.findViewById(R.id.email_input);
        fullname = rootView.findViewById(R.id.fullname_input);
        password = rootView.findViewById(R.id.password_input);
        confirmBtn = rootView.findViewById(R.id.signup_btn);
        mAuth = FirebaseAuth.getInstance();

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String name = fullname.getText().toString();
                String pass = password.getText().toString();
                // Sign up new user
                mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("log", "success");

                            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (fbUser != null) {
                                String name = fbUser.getDisplayName();
                                String userUid = fbUser.getUid();
                                Uri userPhoto = fbUser.getPhotoUrl();
                                // TODO: Add more fields

                                // Saving to DB
                                User user = new User(name, userUid, userPhoto);
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                mDatabase.child("users").child(userUid).setValue(user);

                                // Send name+photoUrl+token(id) to mainActivity to display after signup
                                EventBus.getDefault().post(new MessageUserEvent(user));
                                getActivity().onBackPressed(); // Close fragment
                            }
                        } else {
                            Log.d("log", "failed to sign up");
                            Toast.makeText(getContext(), "failed to sign up", Toast.LENGTH_SHORT).show();
                            // TODO: Add explanation why the sign up failed
                        }

                    }
                });
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
