package com.tsk.thanks4giving;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.xw.repo.BubbleSeekBar;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class FiltersFragment extends Fragment implements LocationListener {

    SwipeRefreshLayout refreshLayout;
    BubbleSeekBar bubbleSeekBar;

    EditText keyword;
    MaterialButton apply_btn, location_btn;
    LovelyProgressDialog progressDialog;
    AutoCompleteTextView categoryDropdown;
    int category = -1, LOCATION_PERMISSION_REQUEST = 2;
    Location location_original = new Location("dummyProvider");
    LocationManager manager;

    public FiltersFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filters, container, false);
        refreshLayout = rootView.findViewById(R.id.refresh);
        keyword = rootView.findViewById(R.id.keyword_et);
        apply_btn = rootView.findViewById(R.id.apply_filters_btn);
        location_btn = rootView.findViewById(R.id.location_filter_btn);
        categoryDropdown = rootView.findViewById(R.id.category_spinner_filter_et);
        bubbleSeekBar = rootView.findViewById(R.id.BubbleSeekBar);
        manager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        final String[] categories = getResources().getStringArray(R.array.categories_for_filter);

        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_categories_item, categories);
        categoryDropdown.setAdapter(adapter);
        // Dumb way to bind the selected item to it's value
        categoryDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                category = position - 1;
            }
        });

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new LovelyProgressDialog(getContext())
                        .setTopColorRes(R.color.colorPrimary)
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .setTitle(R.string.location_loading)
                        .setMessage(R.string.location_permission);

                progressDialog.show(); // start loading dialog

                location_btn.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT >= 23) { // check permissions for gps location and get it.
                    int hasLocationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    } else {
                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, FiltersFragment.this);
                    }
                } else
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, FiltersFragment.this);
            }
        });

        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("keyword", keyword.getText().toString());
                bundle.putInt("category", category);
                bundle.putDouble("lat", location_original.getLatitude());
                bundle.putDouble("long", location_original.getLongitude());
                bundle.putInt("distance", bubbleSeekBar.getProgress() * 1000);
                bundle.putInt("flag", 2);
                RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
                recyclerViewFragment.setArguments(bundle);
                transaction.replace(R.id.flContent, recyclerViewFragment, "ff").addToBackStack(null).commit();
            }
        });

        return rootView;
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.dismiss();
        location_original.setLatitude(location.getLatitude());
        location_original.setLongitude(location.getLongitude());
        if (location_original != null) {
            manager.removeUpdates(this);
        }
        bubbleSeekBar.setVisibility(View.VISIBLE);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // permission function
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showSettingsDialog(getString(R.string.location_permission));
            } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Request location updates:
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, FiltersFragment.this);
            }
        }
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
}