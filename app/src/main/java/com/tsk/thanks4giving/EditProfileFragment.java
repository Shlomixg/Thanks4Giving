package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
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

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    String TAG = "Profile Frag";

    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int PICK_IMAGE = 3;
    CircleImageView profile_image;
    TextInputEditText fullname_et, address_et;
    MaterialButton camera_btn, gallery_btn, save_btn, cancel_btn;
    AutoCompleteTextView genderDropdown;
    String coordinates, randomKey, profile_photo_path;
    File file;
    Uri imageUri;
    int gender, flag_location;

    LovelyProgressDialog progressLoadingDialog;

    FirebaseUser fbUser;
    DatabaseReference mDatabase;
    DatabaseReference ref;
    StorageReference storageReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressLoadingDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_like)
                .setTitle(R.string.dialog_loading_title)
                .setMessage(R.string.dialog_loading_msg);
        progressLoadingDialog.show();

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        profile_image = rootView.findViewById(R.id.edit_profile_user_image);
        fullname_et = rootView.findViewById(R.id.edit_profile_name_et);
        address_et = rootView.findViewById(R.id.edit_profile_user_address_et);
        genderDropdown = rootView.findViewById(R.id.edit_profile_gender_dropdown);
        camera_btn = rootView.findViewById(R.id.edit_profile_camera_btn);
        gallery_btn = rootView.findViewById(R.id.edit_profile_gallery_btn);
        save_btn = rootView.findViewById(R.id.edit_profile_save_btn);
        cancel_btn = rootView.findViewById(R.id.edit_profile_cancel_btn);

        final String[] GENDERS = getResources().getStringArray(R.array.genders);

        // Loading existing data into UI
        ref = mDatabase.child("users").child(fbUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    fullname_et.setText(user.name);
                    address_et.setText(user.address);
                    gender = user.gender;
                    genderDropdown.setText(GENDERS[gender], false);
                    coordinates = user.coordinates;
                    if (user.profilePhoto != null) {
                        profile_photo_path = user.profilePhoto;
                        Glide.with(getContext()).load(user.profilePhoto).centerCrop().into(profile_image);
                    }
                    progressLoadingDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("EditProfile Log", "--- Profile loading failed. Error: " + error);
                progressLoadingDialog.dismiss();
            }
        });

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

        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_gender_item, GENDERS);
        genderDropdown.setAdapter(adapter);
        // Dumb way to bind the selected item to it's value
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

        mValidation.addValidation(getActivity(), R.id.edit_profile_name_tf, RegexTemplate.NOT_EMPTY, R.string.validate_name);
        mValidation.addValidation(getActivity(), R.id.edit_profile_user_address_tf, "^.{3,}$", R.string.validate_address);
        mValidation.addValidation(getActivity(), R.id.edit_profile_gender_tf, RegexTemplate.NOT_EMPTY, R.string.validate_gender);

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

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mValidation.validate()) {
                    updateUser();
                }
            }
        });
    }

    private void updateUser() {
        final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_giftbox_outline)
                .setTitle(R.string.dialog_updating_user)
                .setMessage(R.string.dialog_loading_msg);
        progressDialog.show();

        String name = fullname_et.getText().toString();
        String address = address_et.getText().toString();

        fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(imageUri).build());
        fbUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(profile_photo_path)).build());

        ref.child("name").setValue(name);
        ref.child("profilePhoto").setValue(profile_photo_path);
        ref.child("gender").setValue(gender);
        ref.child("coordinates").setValue(coordinates);
        ref.child("address").setValue(address);

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        View header = navigationView.getHeaderView(0);
        TextView user_name_tv = header.findViewById(R.id.nav_tv_user_name);
        CircleImageView user_photo_civ = header.findViewById(R.id.nav_profile_image);
        user_name_tv.setText(name);
        Glide.with(getContext())
                .load(profile_photo_path)
                .centerCrop()
                .into(user_photo_civ);
        progressDialog.dismiss();
        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.hi) + name + getString(R.string.signup_success), Snackbar.LENGTH_SHORT).show();
        getActivity().getSupportFragmentManager().popBackStack();
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
                .setIcon(R.drawable.ic_giftbox_outline)
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
            uploadPicture();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            profile_image.setImageURI(imageUri);
            uploadPicture();
        } else if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            address_et.setText(place.getAddress());
            String temp = String.valueOf(place.getLatLng());
            coordinates = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPicture() {
        final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_giftbox_outline)
                .setTitle(R.string.dialog_uploading_title); // TODO: Move to strings
        progressDialog.show();
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
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Progress " + (int) progress + " %");
                if ((int) progress == 100) progressDialog.dismiss();
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
