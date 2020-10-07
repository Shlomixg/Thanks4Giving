package com.tsk.thanks4giving;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.greenrobot.eventbus.EventBus;

public class LocationJobService extends JobService {

    Location location = new Location("dummyProvider");

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("ddd", "job started");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission == PackageManager.PERMISSION_GRANTED) {
                Log.d("ddd", "getting location");
                assert locationManager != null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            } else Log.d("ddd", "no location permission");
        } else {
            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
        jobFinished(params, true);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("ddd", "job on stop");
        return true;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            String longitude = "" + loc.getLongitude();
            Log.d("ddd", longitude);
            String latitude = "" + loc.getLatitude();
            Log.d("ddd", latitude);
            location.setLongitude(Double.parseDouble(longitude));
            location.setLatitude(Double.parseDouble(latitude));
            EventBus.getDefault().post(new MessageEvent(location));
            Log.d("ddd", "Sent message event");
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
}