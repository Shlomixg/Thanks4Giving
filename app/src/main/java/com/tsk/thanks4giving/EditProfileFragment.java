package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Random;

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

    File file;
    Uri imageUri;

    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
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
                if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                } else {
                    Random r = new Random();
                    int low = 10;
                    int high = 1000000;
                    int result = r.nextInt(high - low) + low;
                    file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "" + result + ".jpg"); //eran
                    imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);// eran
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
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
                if (!fullname_et.getText().toString().equals(""))
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullname_et.getText().toString()).setPhotoUri(imageUri).build());
                else
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build());
                ref.child(fbUser.getUid()).child("gender").setValue(gender);
                if (!address_et.getText().toString().equals(""))
                    ref.child(fbUser.getUid()).child("address").setValue(address_et.getText().toString());

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
                Toast.makeText(getActivity(), getString(R.string.need_permissions), Toast.LENGTH_SHORT).show();
                cameraBtn.setVisibility(View.GONE);
                galleryBtn.setVisibility(View.GONE);
            } else {
                cameraBtn.setVisibility(View.VISIBLE);
                galleryBtn.setVisibility(View.VISIBLE);
                Random r = new Random();
                int low = 10;
                int high = 1000000;
                int result = r.nextInt(high - low) + low;
                file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "" + result + ".jpg"); //eran
                imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);// eran
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            userImage.setImageURI(imageUri);
        }
    }
}