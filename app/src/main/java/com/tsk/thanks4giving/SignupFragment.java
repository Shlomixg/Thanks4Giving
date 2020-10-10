package com.tsk.thanks4giving;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupFragment extends Fragment {

    CircleImageView profile_image;
    TextInputEditText fullname_et, address_et, email_et, password_et;
    Button confirm_btn, login_btn;
    AutoCompleteTextView genderEditTextExposedDropdown;
    Uri imageUri;
    String coordinates;
    int flag_location;

    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);
        profile_image = rootView.findViewById(R.id.signup_user_image);
        fullname_et = rootView.findViewById(R.id.signup_fullname_et);
        address_et = rootView.findViewById(R.id.signup_address_et);
        email_et = rootView.findViewById(R.id.signup_email_et);
        password_et = rootView.findViewById(R.id.signup_password_et);
        genderEditTextExposedDropdown = rootView.findViewById(R.id.signup_gender_dropdown);
        confirm_btn = rootView.findViewById(R.id.signup_confirm_btn);
        login_btn = rootView.findViewById(R.id.signup_move_login_btn);


        Places.initialize(getActivity().getApplicationContext(), "AIzaSyCJfTtqHj-BCJl5FPrWnYMmNTbqbL0dZYA");
        address_et.setFocusable(false);
        address_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_location = 1;
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldList).build(getActivity());
                startActivityForResult(intent, 200);
            }
        });



        // TODO: Add selecting or take pictures for image

        String[] GENDERS = new String[]{"Male", "Female", "Other"}; // TODO: Move to strings array
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_gender_item, GENDERS);
        genderEditTextExposedDropdown.setAdapter(adapter);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.flContent, new LoginFragment(), "Login Fragment").addToBackStack(null).commit();
            }
        });

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mail = email_et.getText().toString();
                final String pass = password_et.getText().toString();
                final String name = fullname_et.getText().toString();
                final String address = address_et.getText().toString();
                final String gender = genderEditTextExposedDropdown.getText().toString();

                // TODO: Add validation of form

                // Sign up new user
                mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (fbUser != null) {

                                // TODO: Upload to storage and get data

                                if (imageUri == null) {
                                    if (gender.equals("Female"))
                                        imageUri = Uri.parse("android.resource://com.tsk.thanks4giving/drawable/profile_woman");
                                    else {
                                        imageUri = Uri.parse("android.resource://com.tsk.thanks4giving/drawable/profile_man");
                                    }
                                }

                                // Updating full name & photo
                                fbUser.updateProfile(new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(imageUri)
                                        .setDisplayName(name).build())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                       // Saving to DB
                                                                       String userUid = fbUser.getUid();
                                                                       User user = new User(userUid, name, mail, gender, address,coordinates, imageUri.toString());
                                                                       DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                                                       mDatabase.child("users").child(fbUser.getUid()).setValue(user);

                                                                       NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
                                                                       View header = navigationView.getHeaderView(0);
                                                                       TextView user_name_tv = header.findViewById(R.id.nav_tv_user_name);
                                                                       user_name_tv.setText(fbUser.getDisplayName());
                                                                       Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.hi) + fbUser.getDisplayName() + getString(R.string.signup_success), Snackbar.LENGTH_SHORT).show();
                                                                       getActivity().onBackPressed(); // Close fragment
                                                                   }
                                                               }
                                        );


                            }
                        } else {
                            String error = task.getException().getMessage();
                            Log.d("Signup Log", "--- Sign up failed");
                            Log.d("Signup Log", "--- Error: " + error);
                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.signup_fail) + " " + error, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return rootView;
    }

    public void setSnackbar(FirebaseUser firebaseUser) {
        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.hi) + firebaseUser.getDisplayName() + getString(R.string.signup_success), Snackbar.LENGTH_SHORT).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            address_et.setText(place.getAddress());
            String temp = String.valueOf(place.getLatLng());
            coordinates = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getActivity().getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
