package com.tsk.thanks4giving;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupFragment extends Fragment {


    TextInputEditText fullname_et, address_et, email_et, password_et;
    MaterialButton  confirm_btn, login_btn;
    AutoCompleteTextView genderDropdown;
    int gender, flag_location;
    Uri imageUri;
    String coordinates, profile_photo_path;
    FirebaseAuth mAuth;
    StorageReference storageReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_signup, container, false);
        fullname_et = rootView.findViewById(R.id.signup_fullname_et);
        address_et = rootView.findViewById(R.id.signup_address_et);
        email_et = rootView.findViewById(R.id.signup_email_et);
        password_et = rootView.findViewById(R.id.signup_password_et);
        genderDropdown = rootView.findViewById(R.id.signup_gender_dropdown);
        confirm_btn = rootView.findViewById(R.id.signup_confirm_btn);
        login_btn = rootView.findViewById(R.id.signup_move_login_btn);

        Places.initialize(getContext(), "AIzaSyCJfTtqHj-BCJl5FPrWnYMmNTbqbL0dZYA");
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

        final String[] GENDERS = getResources().getStringArray(R.array.genders);

        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_gender_item, GENDERS);
        genderDropdown.setAdapter(adapter);
        genderDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gender = position;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final AwesomeValidation mValidation = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

        mValidation.addValidation(getActivity(), R.id.signup_fullname_tf, RegexTemplate.NOT_EMPTY, R.string.validate_name);
        mValidation.addValidation(getActivity(), R.id.signup_address_tf, "^.{3,}$", R.string.validate_address);
        mValidation.addValidation(getActivity(), R.id.signup_gender_tf, RegexTemplate.NOT_EMPTY, R.string.validate_gender);
        mValidation.addValidation(getActivity(), R.id.signup_email_tf, Patterns.EMAIL_ADDRESS, R.string.validate_email);
        mValidation.addValidation(getActivity(), R.id.signup_password_tf, "^.{6,}$", R.string.validate_pass);

        AwesomeValidation.disableAutoFocusOnFirstFailure();

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
                if (mValidation.validate()) {
                    register();
                }
            }
        });
    }

    private void register() {
        final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setTitle(R.string.dialog_creating_user)
                .setMessage(R.string.dialog_loading_msg);
        progressDialog.show();

        final String mail = email_et.getText().toString();
        final String pass = password_et.getText().toString();
        final String name = fullname_et.getText().toString();
        final String address = address_et.getText().toString();

        // Sign up new user
        mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (fbUser != null) {

                        if (imageUri == null) {
                            if (gender == 1) // If female
                                profile_photo_path = "https://i.imgur.com/LYqUljv.png";
                            else {
                                profile_photo_path = "https://i.imgur.com/LEKWEA2.png";
                            }
                        }
                        // Updating full name & photo
                        fbUser.updateProfile(new UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(profile_photo_path))
                                .setDisplayName(name).build())
                                .addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // Saving to DB
                                                String userUid = fbUser.getUid();
                                                User user = new User(userUid, name, mail, gender, address, coordinates, profile_photo_path);
                                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                                mDatabase.child("users").child(fbUser.getUid()).setValue(user);

                                                NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
                                                View header = navigationView.getHeaderView(0);
                                                TextView user_name_tv = header.findViewById(R.id.nav_tv_user_name);
                                                CircleImageView user_photo_civ = header.findViewById(R.id.nav_profile_image);
                                                user_name_tv.setText(name);
                                                Glide.with(getContext())
                                                        .load(user.profilePhoto)
                                                        .centerCrop()
                                                        .into(user_photo_civ);
                                                progressDialog.dismiss();
                                                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.hi) + name + getString(R.string.signup_success), Snackbar.LENGTH_SHORT).show();
                                                // Close fragment
                                                FragmentManager fragmentManager = getParentFragmentManager();
                                                List<Fragment> list = fragmentManager.getFragments();
                                                // Get last fragment
                                                for (int i = 0; i < list.size() - 1; i++) {
                                                    fragmentManager.popBackStackImmediate();
                                                }
                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                transaction.replace(R.id.flContent, new RecyclerViewFragment(), "Recycler View Fragment").commit();
                                            }
                                        }
                                );
                    }
                } else {
                    String error = task.getException().getMessage();
                    Log.d("Signup Log", "--- Sign up failed. Error: " + error);
                    progressDialog.dismiss();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.signup_fail) + " " + error, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == AutocompleteActivity.RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            address_et.setText(place.getAddress());
            String temp = String.valueOf(place.getLatLng());
            coordinates = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}