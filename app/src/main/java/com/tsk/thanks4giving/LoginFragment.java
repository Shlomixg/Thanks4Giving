package com.tsk.thanks4giving;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class LoginFragment extends Fragment {

    TextInputEditText email_et, password_et;
    Button confirm_btn, signup_btn;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        email_et = rootView.findViewById(R.id.login_email_et);
        password_et = rootView.findViewById(R.id.login_password_et);
        confirm_btn = rootView.findViewById(R.id.login_confirm_btn);
        signup_btn = rootView.findViewById(R.id.login_move_signup_btn);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final AwesomeValidation mValidation = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

        mValidation.addValidation(getActivity(), R.id.login_email_tf, Patterns.EMAIL_ADDRESS, R.string.validate_name);
        mValidation.addValidation(getActivity(), R.id.login_password_tf, "^.{6,}$", R.string.validate_pass);
        AwesomeValidation.disableAutoFocusOnFirstFailure();

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
                if (mValidation.validate()) {
                    login();
                }
            }
        });
    }

    private void login() {
        String mail = email_et.getText().toString();
        String pass = password_et.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("Login Log", "--- Login Success");
                    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (fbUser != null) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), fbUser.getDisplayName() + getString(R.string.welcome_back), Snackbar.LENGTH_SHORT).show();

                        FragmentManager fragmentManager = getParentFragmentManager();
                        List<Fragment> list = fragmentManager.getFragments();
                        // Get last fragment
                        for (int i = 0; i < list.size() - 1; i++) {
                            fragmentManager.popBackStackImmediate();
                        }
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.flContent, new RecyclerViewFragment(), "Recycler View Fragment").addToBackStack(null).commit();
                    }
                } else {
                    String error = task.getException().getMessage();
                    Log.d("Login Log", "--- Login failed. Error: " + error);
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.login_fail) + " " + error, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}