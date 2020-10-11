package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int PICK_IMAGE = 3;
    CircleImageView profile_image;
    TextInputEditText fullname_et, address_et, email_et, password_et;
    MaterialButton camera_btn, gallery_btn, confirm_btn, login_btn;
    AutoCompleteTextView genderDropdown;
    int gender, flag_location;
    File file;
    Uri imageUri;
    String coordinates, randomKey, profile_photo_path;

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
        profile_image = rootView.findViewById(R.id.signup_user_image);
        fullname_et = rootView.findViewById(R.id.signup_fullname_et);
        address_et = rootView.findViewById(R.id.signup_address_et);
        email_et = rootView.findViewById(R.id.signup_email_et);
        password_et = rootView.findViewById(R.id.signup_password_et);
        genderDropdown = rootView.findViewById(R.id.signup_gender_dropdown);
        camera_btn = rootView.findViewById(R.id.signup_camera_btn);
        gallery_btn = rootView.findViewById(R.id.signup_gallery_btn);
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

        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getContext())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                takePicture();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                showSettingsDialog("Camera"); // TODO: Strings with explanation
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                // TODO: Display dialog with explanation why this permission needed
                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .check();
            }
        });

        gallery_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getContext())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                selectPicture();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                showSettingsDialog("Browse"); // TODO: Strings with explanation
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                // TODO: Display dialog with explanation why this permission needed
                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .check();
            }
        });

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

        final String mail = email_et.getText().toString();
        final String pass = password_et.getText().toString();
        final String name = fullname_et.getText().toString();
        final String address = address_et.getText().toString();

        // Sign up new user
        mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getContext())
                            .setTopColorRes(R.color.colorPrimary)
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_like) // TODO: Change to app icon or wait icon
                            .setTitle(R.string.dialog_creating_user)
                            .setMessage(R.string.dialog_loading_msg);
                    progressDialog.show();

                    final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (fbUser != null) {

                        if (imageUri == null) {
                            if (gender == 1) // If female
                                imageUri = Uri.parse("android.resource://com.tsk.thanks4giving/drawable/profile_woman");
                            else {
                                imageUri = Uri.parse("android.resource://com.tsk.thanks4giving/drawable/profile_man");
                            }
                        }
                        uploadPicture();

                        // Updating full name & photo
                        fbUser.updateProfile(new UserProfileChangeRequest.Builder()
                                .setPhotoUri(imageUri)
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
                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                transaction.replace(R.id.flContent, new RecyclerViewFragment(), "Recycler View Fragment").addToBackStack(null).commit();
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

    public void setSnackbar(FirebaseUser firebaseUser) {
        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.hi) + firebaseUser.getDisplayName() + getString(R.string.signup_success), Snackbar.LENGTH_SHORT).show();
    }

    private void takePicture() {
        Random r = new Random();
        int low = 10, high = 1000000, result = r.nextInt(high - low) + low;
        file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "" + result + ".jpg"); // eran
        imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // eran
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void selectPicture() {
        Intent selectIntent = new Intent();
        selectIntent.setType("image/*");
        selectIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(selectIntent, "Select Image"), PICK_IMAGE);
    }

    public void showSettingsDialog(String explanation) {
        new LovelyStandardDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setPositiveButton(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_like)
                .setTitle(R.string.attention)
                .setMessage(explanation)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK) {
            assert data != null;
            imageUri = data.getData();
            profile_image.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            profile_image.setImageURI(imageUri);
        } else if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            address_et.setText(place.getAddress());
            String temp = String.valueOf(place.getLatLng());
            coordinates = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getActivity().getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPicture() {
        randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("ProfileImages/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        try {
                            downloadFile(randomKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // TODO: Handle unsuccessful uploads
                        Log.d("Signup", "Failed to upload image");
                        Log.d("Signup", "Exception: " + exception);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        });
    }

    private void downloadFile(String randomKey) throws IOException {
        StorageReference imageRef = storageReference.child("ProfileImages").child(randomKey);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profile_photo_path = uri.toString();
                    }
                });
    }

}
