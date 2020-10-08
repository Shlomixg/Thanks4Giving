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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    String TAG = "Profile Frag";

    final int WRITE_PERMISSION_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int PICK_IMAGE = 3;
    CircleImageView userImage;
    TextInputEditText fullname_et, address_et, email_et, password_et;
    Button saveBtn, changePicBtn, cameraBtn, galleryBtn, cancelBtn;
    AutoCompleteTextView genderEditTextExposedDropdown;
    String randomKey, profile_photo_path;
    File file;
    Uri imageUri;
    int flag = 0;

    LovelyProgressDialog progressLoadingDialog;

    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    StorageReference storageReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressLoadingDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_like)
                .setTitle("Loading data...") // TODO: Move to strings
                .setMessage("Please wait");
        progressLoadingDialog.show();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
        storageReference = FirebaseStorage.getInstance().getReference();
        userImage = rootView.findViewById(R.id.edit_profile_user_image);
        fullname_et = rootView.findViewById(R.id.edit_profile_name_et);
        address_et = rootView.findViewById(R.id.edit_profile_user_address_et);
        email_et = rootView.findViewById(R.id.edit_profile_email_et);
        genderEditTextExposedDropdown = rootView.findViewById(R.id.edit_profile_gender_dropdown);
        cameraBtn = rootView.findViewById(R.id.change_pic_camera);
        galleryBtn = rootView.findViewById(R.id.change_pic_gallery);
        saveBtn = rootView.findViewById(R.id.edit_profile_save_btn);
        cancelBtn = rootView.findViewById(R.id.edit_profile_cancel_btn);
        changePicBtn = rootView.findViewById(R.id.edit_profile_picture_btn);

        String[] GENDERS = new String[]{"Male", "Female", "Other"}; // TODO: Move to strings array
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_gender_item, GENDERS);
        genderEditTextExposedDropdown.setAdapter(adapter);

        // Loading existing data into UI
        ref = mDatabase.child("users").child(fbUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    fullname_et.setText(user.name);
                    address_et.setText(user.address);
                    email_et.setText(user.email);
                    genderEditTextExposedDropdown.setText(user.gender, false);
                    if (user.profilePhoto != null) {
                        Glide.with(getActivity()).load(user.profilePhoto).centerCrop().into(userImage);
                    }
                    progressLoadingDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("EditProfile Log", "--- Profile loading failed");
                Log.d("EditProfile Log", "--- Error: " + error);
                // TODO: Add error handling
                progressLoadingDialog.dismiss();
            }
        });

        changePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.pic_btns).setVisibility(View.VISIBLE);
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Check on SDK 23
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

        galleryBtn.setOnClickListener(new View.OnClickListener() {
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


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = fullname_et.getText().toString();
                String gender = genderEditTextExposedDropdown.getText().toString();
                String address = address_et.getText().toString();

                if (!name.isEmpty()) {
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullname_et.getText().toString()).setPhotoUri(imageUri).build());
                    ref.child("name").setValue(name);
                }

                if (imageUri == null) {
                    if (gender.equals("Female"))
                        imageUri = Uri.parse("android.resource://com.tsk.thanks4giving/drawable/profile_woman");
                    else {
                        imageUri = Uri.parse("android.resource://com.tsk.thanks4giving/drawable/profile_man");
                    }
                } else {
                    ref.child("profilePhoto").setValue(profile_photo_path);
                }

                fbUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build());
                ref.child("gender").setValue(gender);
                if (!address.isEmpty()) ref.child("address").setValue(address);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return rootView;
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
            userImage.setImageURI(imageUri);
            uploadPicture();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            userImage.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {
        final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_like) // TODO: Change to app icon or wait icon
                .setTitle("Uploading image..."); // TODO: Move to strings
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
