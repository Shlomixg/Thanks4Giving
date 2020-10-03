package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.File;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    final int WRITE_PERMISSION_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int PICK_IMAGE = 3;
    CircleImageView userImage;
    EditText userName;
    String gender = "Male";
    EditText userAddress;
    Button saveBtn;
    Button changePicBtn;
    Button cameraBtn;
    Button galleryBtn;
    RadioButton male;
    RadioButton female;
    String TAG = "Profile Frag";
    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref = mDatabase.child("users");
    File file;
    Uri imageUri;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_editprofile, container, false);
        userImage = rootView.findViewById(R.id.edit_profile_user_image);
        userName = rootView.findViewById(R.id.edit_profile_user_name_et);
        userAddress = rootView.findViewById(R.id.edit_profile_user_address_et);
        cameraBtn = rootView.findViewById(R.id.change_pic_camera);
        galleryBtn = rootView.findViewById(R.id.change_pic_gallery);

        male = rootView.findViewById(R.id.radio_male);
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "android.resource://com.tsk.thanks4giving/drawable/profile_man";
                if(imageUri == null)
                Glide.with(getActivity()).load(Uri.parse(path)).centerCrop().into(userImage);
                gender = getString(R.string.male);
            }
        });
        female = rootView.findViewById(R.id.radio_female);
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "android.resource://com.tsk.thanks4giving/drawable/profile_woman";
                if(imageUri == null)
                Glide.with(getActivity()).load(Uri.parse(path)).centerCrop().into(userImage);
                gender = getString(R.string.female);
            }
        });

        changePicBtn = rootView.findViewById(R.id.edit_profile_picture_btn);
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

        saveBtn = rootView.findViewById(R.id.edit_profile_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri==null) {
                    String path = null;
                    if(gender.equals("Male"))
                        path = "android.resource://com.tsk.thanks4giving/drawable/profile_man";
                    else if(gender.equals("Female"))
                        path = "android.resource://com.tsk.thanks4giving/drawable/profile_woman";
                    imageUri = Uri.parse(path);
                }
                if (!userName.getText().toString().equals(""))
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userName.getText().toString()).setPhotoUri(imageUri).build());
                else
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build());
                ref.child(fbUser.getUid()).child("gender").setValue(gender);
                if (!userAddress.getText().toString().equals(""))
                    ref.child(fbUser.getUid()).child("address").setValue(userAddress.getText().toString());

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, new ProfileFragment(), TAG).addToBackStack(null).commit();
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