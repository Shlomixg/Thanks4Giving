package com.tsk.thanks4giving;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecyclerViewFragment extends Fragment implements LocationListener {

    final String POST_FRAG = "Post Fragment";
    ArrayList<Post> postList = new ArrayList<>();
    RecyclerView recycler;
    PostAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference posts = database.getReference().child("posts");
    Query query;
    SwipeRefreshLayout refreshLayout;
    BubbleSeekBar bubbleSeekBar;
    private static final String ARG_USER_UID = "userUid";
    private static final String ARG_STATUS = "itemsStatus";
    ImageButton delete_btn;
    private String mUserUid;
    private int mItemsStatus;
    EditText keyword;
    TextView current_text, search_tv, current_search;
    Spinner spinner, timer_spinner;
    Button filter_btn, search_btn, clean_btn, submit_filter_btn, submit_search_btn, filter1, filter2, search_keyword,location_filter;
    ImageButton close_btn, close_search_btn, close_edit_current_filters, close_edit_current_search;
    LinearLayout filters, search, filter_submit, search_submit, linear_current_filter;
    long diff;
    int required_days;
    Location location_original = new Location("dummyProvider");
    LocationManager manager; //##
    int LOCATION_PERMISSION_REQUEST = 2;
     ProgressDialog progressDialog2;
    String inputString2;
    SimpleDateFormat myFormat;


    public RecyclerViewFragment() {
    }

    public static RecyclerViewFragment newInstance(String userUid, String itemsStatus) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_UID, userUid);
        args.putString(ARG_STATUS, itemsStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        query = posts;
        if (getArguments() != null) {
            mUserUid = getArguments().getString(ARG_USER_UID);
            mItemsStatus = getArguments().getInt(ARG_STATUS);
            query = posts.orderByChild("userUid").equalTo(mUserUid);
        }

        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);
         progressDialog2 = new ProgressDialog(getActivity().findViewById(android.R.id.content).getContext());
        progressDialog2.setTitle(getString(R.string.loading));
        takeCurrentTime();
        manager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        close_edit_current_filters = rootView.findViewById(R.id.close_current_filter);
        location_filter=rootView.findViewById(R.id.location_filter);
        location_filter.setOnClickListener(new View.OnClickListener() {

            // TODO: Move to strings
            @Override
            public void onClick(View v) {
                progressDialog2.show();

                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    } else {
                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, RecyclerViewFragment.this);
                    }
                } else
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, RecyclerViewFragment.this);
            }
        });
        close_edit_current_search = rootView.findViewById(R.id.close_current_search);
        close_edit_current_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close_edit_current_search.setVisibility(View.GONE);
                search_keyword.setVisibility(View.GONE);
                current_search.setVisibility(View.GONE);
                search_tv.setVisibility(View.VISIBLE);
                keyword.setVisibility(View.VISIBLE);
                search_submit.setVisibility(View.VISIBLE);


            }
        });
        close_edit_current_filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_submit.setVisibility(View.VISIBLE);
                filters.setVisibility(View.VISIBLE);
                linear_current_filter.setVisibility(View.GONE);
            }
        });
        current_text = rootView.findViewById(R.id.current);
        search_tv = rootView.findViewById(R.id.search_tv);
        current_search = rootView.findViewById(R.id.current_search);
        close_btn = rootView.findViewById(R.id.close_all_filter);
        close_search_btn = rootView.findViewById(R.id.close_search);
        close_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_submit.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
            }
        });
        clean_btn = rootView.findViewById(R.id.clean_filter_btn);
        filter_btn = rootView.findViewById(R.id.filter_btn);
        search_btn = rootView.findViewById(R.id.search_btn);
        search_submit = rootView.findViewById(R.id.linear_submitsearch);
        linear_current_filter = rootView.findViewById(R.id.linear_current_filter);
        search = rootView.findViewById(R.id.linear_search);
        filters = rootView.findViewById(R.id.linear_filter);
        filter_submit = rootView.findViewById(R.id.linear_filter_submit);
        keyword = rootView.findViewById(R.id.keyword);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.setVisibility(View.GONE);
                filter_submit.setVisibility(View.GONE);
                linear_current_filter.setVisibility(View.GONE);
            }
        });
        clean_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                spinner.setSelection(0);
                timer_spinner.setSelection(0);
                filter1.setVisibility(View.GONE);
                filter2.setVisibility(View.GONE);
                current_text.setVisibility(View.GONE);
                close_edit_current_filters.setVisibility(View.GONE);
                linear_current_filter.setVisibility(View.GONE);

//                Toast.makeText(getContext(), "Days: "+daysBetween, Toast.LENGTH_SHORT).show();

            }
        });


        //        linearLayout_filters=rootView.findViewById(R.id.all_filters);
        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linear_current_filter.getVisibility() == View.GONE) {
                    filters.setVisibility(View.VISIBLE);
                    filter_submit.setVisibility(View.VISIBLE);
                    search.setVisibility(View.GONE);
                    search_submit.setVisibility(View.GONE);
                }


            }
        });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search.getVisibility() == View.GONE) {
                    filters.setVisibility(View.GONE);
                    filter_submit.setVisibility(View.GONE);
                    search.setVisibility(View.VISIBLE);
                    search_submit.setVisibility(View.VISIBLE);
                    search_submit.setVisibility(View.VISIBLE);
                }

            }
        });
        spinner = rootView.findViewById(R.id.category_spinner_filter);
        timer_spinner = rootView.findViewById(R.id.time_spinner_filter);
        String[] a = getResources().getStringArray(R.array.categories_for_filter);
        String[] b = getResources().getStringArray(R.array.times);
        final ArrayAdapter<String> adapter1 =
                new ArrayAdapter<String>(getActivity(), R.layout.spinner_text_filter, a) {
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
        adapter1.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(adapter1);
        spinner.setSelection(0, false);
        final ArrayAdapter<String> adapter2 =
                new ArrayAdapter<String>(getActivity(), R.layout.time_spinner_text_filter, b) {
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
        adapter2.setDropDownViewResource(R.layout.spinner_text);
        timer_spinner.setAdapter(adapter2);
        timer_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (timer_spinner.getSelectedItem().toString()) {
                    case "Today":
                        required_days = 0;
                        break;
                    case "Last 3 days":
                        required_days = 2;
                        break;
                    case "Last week":
                        required_days = 6;
                        break;
                    case "All posts":
                        required_days = -1;
                        break;
                    case "Time":
                        required_days = -1;
                        break;
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timer_spinner.setSelection(0);


        submit_filter_btn = rootView.findViewById(R.id.submit_filter);
        filter1 = rootView.findViewById(R.id.filter1);
        filter2 = rootView.findViewById(R.id.filter2);
        search_keyword = rootView.findViewById(R.id.your_search);
        submit_search_btn = rootView.findViewById(R.id.submit_serach);
        submit_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_keyword.setText(keyword.getText().toString());
                search_keyword.setVisibility(View.VISIBLE);
                current_search.setVisibility(View.VISIBLE); //%%
                keyword.setVisibility(View.GONE);
                search_tv.setVisibility(View.GONE);
                search_keyword.animate().rotation(search_keyword.getRotation() + 360).start();
                close_edit_current_search.setVisibility(View.VISIBLE);
                search_submit.setVisibility(View.GONE);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post post = ds.getValue(Post.class);
                               String coordinates = post.coordinates;
                            String a[] = coordinates.split(",");
                            Location location2 = new Location("dummyProvider");
                            location2.setLatitude(Double.parseDouble(a[0]));
                            location2.setLongitude(Double.parseDouble(a[1]));

                            String inputString1 = post.date;
                            try {
                                Date date1 = myFormat.parse(inputString1);
                                Date date2 = myFormat.parse(inputString2);
                                diff = date2.getTime() - date1.getTime();// calculate the difference
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (location_original!=null && location_original.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000) {
                                if (post.desc.contains(keyword.getText().toString())) {
//                                    if (pos.getDesc().contains(keyword.getText().toString()) && (pos.getCategory().equals(spinner.getSelectedItem()) || spinner.getSelectedItem().equals("All Categories")) ) {
//                                    if (required_days != -1) {
//
////                                        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
//                                            postList.add(pos);

//                                        }
//                                    } else {
                                    postList.add(post);
//                                    }
                                }
                            }
                            else    if (post.desc.contains(keyword.getText().toString())) { // in case - no location
                                postList.add(post);

                            }
                            }



                        Collections.reverse(postList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        submit_filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        if (spinner.getItemAtPosition(0).equals(spinner.getSelectedItem()) &&
                                !(timer_spinner.getItemAtPosition(0).equals(timer_spinner.getSelectedItem()))) {
                            linear_current_filter.setVisibility(View.VISIBLE);
                            filter2.setText(timer_spinner.getSelectedItem().toString());
                            filter2.animate().rotation(filter2.getRotation() + 360).start();
                            filter1.setVisibility(View.GONE);
                            filter2.setVisibility(View.VISIBLE);
                            close_edit_current_filters.setVisibility(View.VISIBLE);
                            current_text.setVisibility(View.VISIBLE);

                        } else if (!(spinner.getItemAtPosition(0).equals(spinner.getSelectedItem())) &&
                                timer_spinner.getItemAtPosition(0).equals(timer_spinner.getSelectedItem())) {
                            linear_current_filter.setVisibility(View.VISIBLE);
                            filter1.setText(spinner.getSelectedItem().toString());
                            filter1.animate().rotation(filter1.getRotation() + 360).start();
                            filter2.setVisibility(View.GONE);
                            filter1.setVisibility(View.VISIBLE);
                            close_edit_current_filters.setVisibility(View.VISIBLE);
                            current_text.setVisibility(View.VISIBLE);


                        } else if (!(spinner.getItemAtPosition(0).equals(spinner.getSelectedItem())) &&
                                !(timer_spinner.getItemAtPosition(0).equals(timer_spinner.getSelectedItem()))) {
                            linear_current_filter.setVisibility(View.VISIBLE);
                            filter1.setVisibility(View.VISIBLE);
                            filter2.setVisibility(View.VISIBLE);
                            current_text.setVisibility(View.VISIBLE);
                            close_edit_current_filters.setVisibility(View.VISIBLE);
                            filter1.setText(spinner.getSelectedItem().toString());
                            filter1.animate().rotation(filter1.getRotation() + 360).start();
                            filter2.setText(timer_spinner.getSelectedItem().toString());
                            filter2.animate().rotation(filter2.getRotation() + 360).start();

                        } else if (spinner.getItemAtPosition(0).equals(spinner.getSelectedItem()) &&
                                timer_spinner.getItemAtPosition(0).equals(timer_spinner.getSelectedItem())) {
                            filter1.setVisibility(View.GONE);
                            filter2.setVisibility(View.GONE);
                            current_text.setVisibility(View.GONE);
                            linear_current_filter.setVisibility(View.GONE);
                            close_edit_current_filters.setVisibility(View.GONE);
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post post = ds.getValue(Post.class);

                            String coordinates = post.coordinates;
                            String a[] = coordinates.split(",");
                            Location location2 = new Location("dummyProvider");
                            location2.setLatitude(Double.parseDouble(a[0]));
                            location2.setLongitude(Double.parseDouble(a[1]));
                            String inputString1 = post.date; // take post time
                            try {
                                Date date1 = myFormat.parse(inputString1);
                                Date date2 = myFormat.parse(inputString2);
                                diff = date2.getTime() - date1.getTime(); // calculate the difference
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (location_original != null) {
                                if (location_original.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
                                    if (post.category.equals(spinner.getSelectedItem()) || spinner.getItemAtPosition(0).equals(spinner.getSelectedItem())) {
                                        if (required_days != -1) {

                                            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                                                postList.add(post);

                                            }
                                        } else {
                                            postList.add(post);

                                        }
                                    } else if (post.desc.contains(keyword.getText().toString()) && (spinner.getSelectedItem().equals("All Categories") || spinner.getItemAtPosition(0).equals(spinner.getSelectedItem()))) {
                                        if (required_days != -1) {

                                            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                                                postList.add(post);

                                            }
                                        } else {
                                            postList.add(post);

                                        }


                                    }
                            }
                            else
                            {

                            }
                        }

                        Collections.reverse(postList);
                        adapter.notifyDataSetChanged();
                        filter_submit.setVisibility(View.GONE);
                        filters.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });


        bubbleSeekBar = (BubbleSeekBar) rootView.findViewById(R.id.BubbleSeekBar);
        bubbleSeekBar.setProgress((float) (100.0));
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(final BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        filterLocation(snapshot);//                    {
//                        holder.edit_btn.setVisibility(View.VISIBLE);
//                    }
//                        }
                        Collections.reverse(postList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        refreshLayout = rootView.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        showallposts(snapshot);
                        Collections.reverse(postList);
                        // adapter=new PostAdapter(postList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        });

        final String path = "android.resource://com.tsk.thanks4giving/drawable/ic_home"; //TODO ???

        final ProgressDialog progressDialog = new ProgressDialog(getActivity().findViewById(android.R.id.content).getContext());
        progressDialog.setTitle(getString(R.string.loading));


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                postList.clear();
                showallposts(snapshot);
                Collections.reverse(postList);
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        recycler = rootView.findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(postList);
        recycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setListener(new PostAdapter.PostClickListener() {
            @Override
            public void onClickListener(int pos, View v) {
                String postId = postList.get(pos).postID;
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("PostId", postId);
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(bundle);
                transaction.replace(R.id.flContent, postFragment, POST_FRAG).addToBackStack(null).commit();
            }

            @Override
            public void onLongClickListener(int pos, View v) {
            }
        });

        recycler.setAdapter(adapter);
        return rootView;
    }

    private void takeCurrentTime() {
        myFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
         inputString2 = myFormat.format(date);
    }

    private void showallposts(DataSnapshot snapshot) {
        for (DataSnapshot ds : snapshot.getChildren()) {
            Post post = ds.getValue(Post.class);
            String coordinates = post.coordinates;
            String a[] = coordinates.split(",");
            Location location2 = new Location("dummyProvider");
            location2.setLatitude(Double.parseDouble(a[0]));
            location2.setLongitude(Double.parseDouble(a[1]));
//            if (location_original.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
            postList.add(post);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d("ddd", "event reached fragment");
        //TODO refresh posts list with new location
        location_original.setLongitude(event.location.getLongitude());
        location_original.setLatitude(event.location.getLatitude());

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void filterLocation(DataSnapshot snapshot) {
        for (DataSnapshot ds : snapshot.getChildren()) {
            Post post = ds.getValue(Post.class);
            String coordinates = post.coordinates;
            String a[] = coordinates.split(",");
            Location location2 = new Location("dummyProvider");
            location2.setLatitude(Double.parseDouble(a[0]));
            location2.setLongitude(Double.parseDouble(a[1]));
            if (location_original.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
                postList.add(post);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog2.dismiss();
        location_original.setLatitude(location.getLatitude());
        location_original.setLongitude(location.getLongitude());
      Toast.makeText(getContext(), "Location: "+location_original.getLatitude()+","+location_original.getLongitude(), Toast.LENGTH_SHORT).show();
        if (location_original!=null)
        {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                showSettingsDialog(getString(R.string.location_permission));
            } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Request location updates:
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, RecyclerViewFragment.this);
            }
        }
    }
}