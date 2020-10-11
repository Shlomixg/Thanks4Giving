package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class NewPostFragment extends Fragment implements LocationListener, AdapterView.OnItemSelectedListener {

    final String RECYCLER_FRAG = "Recycler View Fragment";
    private static final String ARG_POST_ID = "postID";
    private String mPostID;
    final int LOCATION_PERMISSION_REQUEST = 2;
    static final int PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;

    ImageView image;
    TextInputEditText item_name_et, item_desc_et, address_et;
    MaterialButton gps_btn, default_address_btn, camera_btn, browse_btn, confirm_btn;
    AutoCompleteTextView categoryDropdown;
    LovelyProgressDialog progressDialog;
    int category, flag_location = 0;
    Handler handler = new Handler();
    Geocoder geocoder;
    LocationManager manager;
    File file;
    Uri imageUri;
    String image_path, randomKey;
    String location_method, coordinates, userUid;

    FirebaseUser currentFBUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference users = database.getReference("users");
    DatabaseReference posts = database.getReference("posts");
    private StorageReference storageReference;

    public static NewPostFragment newInstance(String userUid, String itemsStatus) {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, userUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext());
        manager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        currentFBUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        userUid = currentFBUser.getUid();

        List<String> providers = manager.getProviders(false);
        for (String provider : providers) {
            Log.d(provider, "enabled:" + manager.isProviderEnabled(provider));
        }

        Criteria criteria = new Criteria();
        criteria.setSpeedRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        if (getArguments() != null) {
            progressDialog = new LovelyProgressDialog(getContext())
                    .setTopColorRes(R.color.colorPrimary)
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_launcher_foreground)
                    .setTitle(R.string.dialog_loading_title)
                    .setMessage(R.string.dialog_loading_msg);
            progressDialog.show();

            mPostID = getArguments().getString(ARG_POST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_new_post, container, false);
        item_name_et = rootView.findViewById(R.id.post_name_et);
        item_desc_et = rootView.findViewById(R.id.post_desc_et);
        address_et = rootView.findViewById(R.id.post_pickup_address_et);
        gps_btn = rootView.findViewById(R.id.post_gps_btn);
        default_address_btn = rootView.findViewById(R.id.post_default_address_btn);
        categoryDropdown = rootView.findViewById(R.id.post_category_spinner);
        image = rootView.findViewById(R.id.post_item_image);
        browse_btn = rootView.findViewById(R.id.post_gallery_btn);
        camera_btn = rootView.findViewById(R.id.post_camera_btn);
        confirm_btn = rootView.findViewById(R.id.post_confirm_btn);

        final String[] categories = getResources().getStringArray(R.array.categories);

        // Loading data of existing post (which is edited)
        if (mPostID != null && !mPostID.isEmpty()) {
            posts.child(mPostID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        item_name_et.setText(post.title);
                        item_desc_et.setText(post.desc);
                        address_et.setText(post.address);
                        coordinates = post.coordinates;
                        category = post.category;
                        categoryDropdown.setText(categories[category], false);
                        image_path = post.postImage;
                        Glide.with(getContext()).load(post.postImage).centerCrop().into(image);

                        progressDialog.dismiss();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

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
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_categories_item, categories);
        categoryDropdown.setAdapter(adapter);
        categoryDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                category = position;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final AwesomeValidation mValidation = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

        mValidation.addValidation(getActivity(), R.id.post_name_tf, "^.{3,}$", R.string.validate_item_name);
        mValidation.addValidation(getActivity(), R.id.post_desc_tf, RegexTemplate.NOT_EMPTY, R.string.validate_desc);
        mValidation.addValidation(getActivity(), R.id.post_category_tf, RegexTemplate.NOT_EMPTY, R.string.validate_category);

        AwesomeValidation.disableAutoFocusOnFirstFailure();

        gps_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_location = 0;
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    } else {
                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, NewPostFragment.this);
                    }
                } else
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, NewPostFragment.this);
            }
        });

        default_address_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_location = 1;
                users.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            address_et.setText(user.address);
                            coordinates = user.coordinates;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

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
                                showSettingsDialog(getString(R.string.camera_permissions));
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .check();
            }
        });

        browse_btn.setOnClickListener(new View.OnClickListener() {
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
                                showSettingsDialog(getString(R.string.gallery_permissions));
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .check();
            }
        });

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mValidation.validate()) {
                    final String postID = (mPostID == null || mPostID.isEmpty()) ? posts.push().getKey() : mPostID;
                    uploadPost(postID);
                }
            }
        });
    }

    public void uploadPost(final String postID) {
        LovelyProgressDialog uploadProgressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setTitle(getString(R.string.dialog_uploading_post))
                .setMessage(R.string.dialog_loading_msg)
                .setIcon(R.drawable.ic_launcher_foreground);
        uploadProgressDialog.show();

        if (flag_location == 0) location_method = "GPS";
        else location_method = "Google";

        final String item_name = item_name_et.getText().toString();
        final String item_desc = item_desc_et.getText().toString();
        final String address = address_et.getText().toString();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        final Post post = new Post(postID, userUid, item_name, item_desc, address, coordinates, location_method, format.format(date), 1, category, image_path);
        posts.child(postID).setValue(post);

        // Add post id to user data
        users.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.postsUid == null) user.postsUid = new ArrayList<>();
                    user.postsUid.add(postID);
                    users.child(userUid).setValue(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        uploadProgressDialog.dismiss();
        setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
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
                .setIcon(R.drawable.ic_launcher_foreground)
                .setTitle(R.string.attention)
                .setMessage(explanation)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        coordinates = lat + "," + lng;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    final Address bestAddress = addresses.get(0);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            address_et.setText(bestAddress.getThoroughfare() + "," + bestAddress.getFeatureName() + "," + bestAddress.getLocality() + "," + bestAddress.getCountryName());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showSettingsDialog(getString(R.string.location_permission));
            } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Request location updates:
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, NewPostFragment.this);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK) {
            imageUri = data.getData();
            image.setImageURI(imageUri);
            uploadPicture();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            image.setImageURI(imageUri);
            uploadPicture();
        }

        if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            address_et.setText(place.getAddress());

            String temp = String.valueOf(place.getLatLng());
            coordinates = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
        }
    }

    private void setFragment(Fragment fragment, String FRAG) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flContent, fragment, FRAG);
        if (!FRAG.equals(RECYCLER_FRAG)) transaction.addToBackStack(null);
        transaction.commit();
    }

    private void uploadPicture() {
        final LovelyProgressDialog uploadPicProgressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setTitle(R.string.dialog_uploading_title)
                .setMessage(R.string.dialog_loading_msg);
        uploadPicProgressDialog.show();
        randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
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
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                uploadPicProgressDialog.setMessage("Progress " + (int) progress + " %");
                if ((int) progress == 100)
                    uploadPicProgressDialog.dismiss();
            }
        });
    }

    private void downloadFile(String randomKey) throws IOException {
        StorageReference imageRef = storageReference.child("images").child(randomKey);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        image_path = uri.toString();
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
