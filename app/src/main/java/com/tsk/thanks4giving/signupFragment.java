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

public class signupFragment extends Fragment {

    EditText email;
    EditText fullname;
    EditText password;
    Button confirmBtn;
    FirebaseAuth mAuth;
    String name;

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
        View rootView = inflater.inflate(R.layout.signup_fragment_layout,container,false);
        email = rootView.findViewById(R.id.email_input);
        fullname = rootView.findViewById(R.id.fullname_input);
        password = rootView.findViewById(R.id.password_input);
        confirmBtn = rootView.findViewById(R.id.signup_btn);
        mAuth = FirebaseAuth.getInstance();

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail  = email.getText().toString();
                name = fullname.getText().toString();
                String pass = password.getText().toString();
                //sign up new user
                mAuth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Log.d("log","success");

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String name = user.getDisplayName();
                            Log.d("log"," "+name);
                            String userToken = user.getIdToken(true).toString();

                            User newUser = new User(name, userToken);

                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("users").child(userToken).setValue(user);

                            //send name+photoUrl+token(id) to mainActivity to display after signup
                            EventBus.getDefault().post(new MessageEvent(name, userToken));
                            getActivity().onBackPressed(); //close fragment
                        }
                        else
                            Log.d("log","fail");
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
