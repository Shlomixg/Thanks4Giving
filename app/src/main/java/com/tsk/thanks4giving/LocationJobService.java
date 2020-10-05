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

    String CO;

    @Override
    public boolean onStartJob(JobParameters params) {
        int id = params.getJobId();
        Log.d("ddd","job " + id + " started");
        getCoordinates();
        String a[] = CO.split(",");
        Location location = new Location("dummyProvider");
        location.setLatitude(Double.parseDouble(a[1]));
        location.setLongitude(Double.parseDouble(a[0]));
        EventBus.getDefault().post(new MessageEvent(location));
        return true;
    }

    private void getCoordinates() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("ddd","thread running");
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                LocationListener locationListener = new MyLocationListener();
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("ddd","has permission");
                        assert locationManager != null;
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                    }
                    else Log.d("ddd","has no permission");
                }
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("ddd","Job canceled before finishing");
        return true;
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            Log.d("ddd","location listener activated");
            String longitude = "" + loc.getLongitude();
            String latitude = "" + loc.getLatitude();
            CO = longitude+latitude;
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