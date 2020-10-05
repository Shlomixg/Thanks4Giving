package com.tsk.thanks4giving;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    TextInputEditText email_et, password_et;
    Button forgot_pass_btn, confirm_btn, signup_btn;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        email_et = rootView.findViewById(R.id.login_email_et);
        password_et = rootView.findViewById(R.id.login_password_et);
        forgot_pass_btn = rootView.findViewById(R.id.login_forgot_pass_btn);
        confirm_btn = rootView.findViewById(R.id.login_confirm_btn);
        signup_btn = rootView.findViewById(R.id.login_move_signup_btn);

        forgot_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Take care of forgot password
                Snackbar.make(getActivity().findViewById(android.R.id.content), "TODO: Forgot Password", Snackbar.LENGTH_SHORT).show();
            }
        });

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.flContent, new SignupFragment(), "Signup Fragment").addToBackStack(null).commit();
            }
        });

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email_et.getText().toString();
                String pass = password_et.getText().toString();

                // TODO: Add validation of form

                firebaseAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Login Log", "--- Login Success");
                            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (fbUser != null) {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), fbUser.getDisplayName() + getString(R.string.wellcome_back), Snackbar.LENGTH_SHORT).show();
                                getActivity().onBackPressed(); // Close fragment
                            }
                        } else {
                            // TODO: Handle case of wrong pass
                            String error = task.getException().getMessage();
                            Log.d("Login Log", "--- Login failed");
                            Log.d("Login Log", "--- Error: " + error);
                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.login_fail) + " " + error, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return rootView;
    }

/*
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    */
}