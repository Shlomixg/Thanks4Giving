package com.tsk.thanks4giving;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

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
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

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
    String randomKey;
    String path2;
    File file;
    Uri imageUri;
    int flag = 0;


    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_gender_item, GENDERS);
        genderEditTextExposedDropdown.setAdapter(adapter);

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                flag = 1;
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                    } else {
                        Random r = new Random();
                        int low = 10, high = 1000000;
                        int result = r.nextInt(high - low) + low;
                        file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "" + result + ".jpg"); //eran
                        imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);// eran
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 2;
                if (Build.VERSION.SDK_INT >= 23) {

                    if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
                    }
                }
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
                String gender = genderEditTextExposedDropdown.getText().toString();
                if (imageUri == null) {
                    String path = null;
                    if (gender.equals("Female"))
                        path = "android.resource://com.tsk.thanks4giving/drawable/profile_woman";
                    else
                        path = "android.resource://com.tsk.thanks4giving/drawable/profile_man";
                    imageUri = Uri.parse(path);

                }
                ref.child("profilePhoto").setValue(path2);
                if (!fullname_et.getText().toString().equals(""))
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullname_et.getText().toString()).setPhotoUri(imageUri).build());
                else
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build());
                ref.child("gender").setValue(gender);
                if (!address_et.getText().toString().equals(""))
                    ref.child("address").setValue(address_et.getText().toString());
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.attention)).setMessage(getString(R.string.location_permission))
                        .setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:"+getActivity().getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // finish();
                            }
                        }).setCancelable(false).show();

            } else {
                {
                    cameraBtn.setVisibility(View.VISIBLE);
                    galleryBtn.setVisibility(View.VISIBLE);
                    Random r = new Random();
                    int low = 10;
                    int high = 1000000;
                    int result = r.nextInt(high - low) + low;
                    file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "" + result + ".jpg"); //eran
                    imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                    if (flag == 1) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);// eran
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else if (flag == 2) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK) {
            assert data != null;
            imageUri = data.getData();
            userImage.setImageURI(imageUri);
            uploadPicture();
//            uploadPicture();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            userImage.setImageURI(imageUri);
           uploadPicture();
        }
    }

    private void uploadPicture() {
        final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getActivity());
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();
        randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("ProfileImages/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();

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
                        // Handle unsuccessful uploads
                        // ...
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Progress " + (int) progress + " %");
                if ((int) progress == 100)
                    progressDialog.dismiss();
            }
        });
    }

    private void downloadFile(String randomKey) throws IOException {
        StorageReference imageRef = storageReference.child("ProfileImages").child(randomKey);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(getContext(), "abbb", Toast.LENGTH_SHORT).show(); // for testing
                        path2 = uri.toString();
                    }
                });
//    }
    }
}
