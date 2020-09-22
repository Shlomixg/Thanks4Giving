package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.List;

public class newPostFragment extends Fragment implements LocationListener {
    final int LOCATION_PERMISSION_REQUEST =1 ;
    EditText coordinateTv;
    EditText addressTv;
    Handler handler = new Handler();//##
    Geocoder geocoder; //##
    LocationManager manager;//##
    Button btn_gps;//##

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext());

        if(Build.VERSION.SDK_INT>=23) {

            int hasLocationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasLocationPermission!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
            }
        }

        manager = (LocationManager)getActivity().getSystemService(getContext().LOCATION_SERVICE);

        List<String> providers = manager.getProviders(false);
        for(String provider:providers) {
            Log.d(provider,"enabled:" + manager.isProviderEnabled(provider));
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
        View rootView = inflater.inflate(R.layout.new_post_layout,container,false);
        coordinateTv=rootView.findViewById(R.id.condition_editText);//##
        addressTv=rootView.findViewById(R.id.address_editText);//##
        btn_gps=rootView.findViewById(R.id.gpsLocation_btn);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(),"dddddddd",Toast.LENGTH_SHORT).show();
                if(Build.VERSION.SDK_INT>=23) {
                    int hasLocationPermission = ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    }
                    else {
                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,100,newPostFragment.this);
                    }
                }
                else
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,100,newPostFragment.this);

            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onLocationChanged(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();

        coordinateTv.setText(lat + " , " + lng);


        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
                    final Address bestAddress = addresses.get(0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            addressTv.setText(bestAddress.getCountryName() + ","
                                    + bestAddress.getFeatureName() + " , " +bestAddress.getThoroughfare() + " , " +bestAddress.getSubThoroughfare()
                                    +"," +bestAddress.getAdminArea()  + " , "+ bestAddress.getLocality() + "," + bestAddress.getSubLocality());
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_REQUEST) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Attention").setMessage("The application must have location permission in order for it to work!")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                // intent.setData(Uri.parse("package:"+getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //finish();
                            }
                        }).setCancelable(false).show();
            }
        }
    }
}
