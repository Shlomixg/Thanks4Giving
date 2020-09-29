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

public class LoginFragment extends Fragment {

    EditText email;
    EditText password;
    Button loginBtn;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        email = rootView.findViewById(R.id.login_email_input);
        password = rootView.findViewById(R.id.login_password_input);
        loginBtn = rootView.findViewById(R.id.login_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String pass = password.getText().toString();

                firebaseAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (fbUser != null) {
                                String userUid = fbUser.getUid();

                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                // TODO: get user from DB (with the uid) and send it to mainActivity
                                Toast.makeText(getContext(), "Login Success", Toast.LENGTH_LONG).show();

                                // Send name+photoUrl+token(id) to mainActivity to display after log in
                                getActivity().onBackPressed(); // Close fragment
                            }

                        } else {
                            Log.d("log", "fail");
                            Toast.makeText(getContext(), "failed to login", Toast.LENGTH_SHORT).show();
                            // TODO: Add explanation why the login failed
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