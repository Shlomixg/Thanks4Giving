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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                String pass = password.getText().toString();
                // Sign up new user
                mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("log", "signup success");

                            final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (fbUser != null) {
                                final String name = fullname.getText().toString();
                                String userUid = fbUser.getUid();
                                Uri userPhoto = null; // TODO: add image view and take photo
                                // TODO: Add more fields

                                // Updating full name & photo
                                fbUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(null).setDisplayName(name).build())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                       NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
                                                                       View header = navigationView.getHeaderView(0);
                                                                       TextView user_name_tv = header.findViewById(R.id.nav_tv_user_name);
                                                                       user_name_tv.setText(fbUser.getDisplayName());
                                                                       setSnackbar(fbUser);
                                                                       getActivity().onBackPressed(); // Close fragment
                                                                   }
                                                               }
                                        );

                                // Saving to DB
                                User user = new User(name, userUid);
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                mDatabase.child("users").child(userUid).setValue(user);
                            }
                        } else {
                            Log.d("log", "sign up failed");
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Sign up failed", Snackbar.LENGTH_SHORT).show();
                            // TODO: Add explanation why the sign up failed
                        }

                    }
                });
            }
        });
        return rootView;
    }

    public void setSnackbar(FirebaseUser firebaseUser) {
        Snackbar.make(getActivity().findViewById(android.R.id.content), "Hi " + firebaseUser.getDisplayName() + ", Sign up successful", Snackbar.LENGTH_SHORT).show();
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
