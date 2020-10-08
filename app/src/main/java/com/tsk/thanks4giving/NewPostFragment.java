package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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
    final int WRITE_PERMISSION_REQUEST = 1, LOCATION_PERMISSION_REQUEST = 2;
    static final int PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;
    int flag_location = 0;
    EditText descriptionET, addressTv;
    Handler handler = new Handler(); //##
    Geocoder geocoder; //##
    LocationManager manager; //##
    Button btn_gps, camera_btn, browse_btn; //##
    File file;
    Uri imageUri;
    ImageView image;
    Spinner spinner;
    ImageButton confirm_btn; //%
    final String RECYCLER_FRAG = "Recycler View Fragment";
    String path, path2;
    String coordinates, location_method, randomKey;

    FirebaseUser currentFBUser;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference users = database.getReference("users");
    DatabaseReference posts = database.getReference("posts");
    private StorageReference storageReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext());
        storageReference = FirebaseStorage.getInstance().getReference();

        manager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_post, container, false);
        browse_btn = rootView.findViewById(R.id.gallery_btn);
        camera_btn = rootView.findViewById(R.id.pic_btn);
        addressTv = rootView.findViewById(R.id.address_editText);
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyCJfTtqHj-BCJl5FPrWnYMmNTbqbL0dZYA");
        addressTv.setFocusable(false);
        addressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_location = 1;
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldList).build(getActivity());
                startActivityForResult(intent, 200);
            }
        });
        btn_gps = rootView.findViewById(R.id.gpsLocation_btn);
        descriptionET = rootView.findViewById(R.id.condition_editText); //##
        image = rootView.findViewById(R.id.newPostImage);
        spinner = rootView.findViewById(R.id.category_spinner);
        confirm_btn = rootView.findViewById(R.id.confirm_btn);

        currentFBUser = mAuth.getCurrentUser();

        String[] a = getResources().getStringArray(R.array.categories);

        final ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, a) {
                    @Override
                    public boolean isEnabled(int position) {
                        // Disable the second item from Spinner
                        return position != 0;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        if (position == 0) {
                            // Set the disable item text color
                            tv.setTextColor(Color.GRAY);
                        } else {
                            tv.setTextColor(Color.BLACK);
                        }
                        return view;
                    }
                };
        adapter.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(0, false);

        btn_gps.setOnClickListener(new View.OnClickListener() {
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

        camera_btn.setOnClickListener(new View.OnClickListener() {
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

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                path = "android.resource://com.tsk.thanks4giving/" + R.drawable.profile_man; // here

                final String uid = currentFBUser.getUid();
                // TODO: Add title & desc to post
                final String postID = posts.push().getKey();

                if (flag_location == 0) location_method = "GPS";
                else location_method = "Google";

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                String date1 = format.format(date);


                final Post post = new Post(postID, uid, descriptionET.getText().toString(), addressTv.getText().toString(), coordinates, location_method, date1, 1, spinner.getSelectedItem().toString(), path2);
                posts.child(postID).setValue(post);

                // Save post id in user data
                users.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.postsUid == null) user.postsUid = new ArrayList<>();
                            user.postsUid.add(postID);
                            users.child(uid).setValue(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                setFragment(new RecyclerViewFragment(), RECYCLER_FRAG);
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
    public void onLocationChanged(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        coordinates = lat + "," + lng;
        // descriptionET.setText(lat + " , " + lng);
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
                            addressTv.setText(bestAddress.getThoroughfare() + "," + bestAddress.getFeatureName() + "," + bestAddress.getLocality() + "," + bestAddress.getCountryName());
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
            addressTv.setText(place.getAddress());
            String temp = String.valueOf(place.getLatLng());
            coordinates = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getActivity().getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
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
        final LovelyProgressDialog progressDialog = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_like) // TODO: Change to app icon or wait icon
                .setTitle("Uploading image..."); // TODO: Move to strings
        progressDialog.show();
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
                progressDialog.setMessage("Progress " + (int) progress + " %"); // TODO: does it work?
                if ((int) progress == 100)
                    progressDialog.dismiss();
            }
        });
    }

    private void downloadFile(String randomKey) throws IOException {
        StorageReference imageRef = storageReference.child("images").child(randomKey);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        path2 = uri.toString();
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
