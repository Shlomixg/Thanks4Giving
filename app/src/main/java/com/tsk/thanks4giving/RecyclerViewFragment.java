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

import com.bumptech.glide.Glide;
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
    Spinner spinner, times_spinner;
    Button filter_btn, search_btn, clean_btn, submit_filter_btn, submit_search_btn, filter1, filter2, search_keyword,location_filter;
    ImageButton close_filter_button, close_search_btn, edit_current_filters, edit_current_search;
    LinearLayout filters, search, filter_submit, search_submit, linear_current_filter;
    long diff;
    int required_days;
    Location location_original = new Location("dummyProvider");
    LocationManager manager; //##
    int LOCATION_PERMISSION_REQUEST = 2;
     LovelyProgressDialog progressDialog2;
    int flagBack=-1;
    final DatabaseReference users = database.getReference().child("users");
    String temp;

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

       progressDialog2 = new LovelyProgressDialog(getContext())
                .setTopColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setIcon(R.drawable.ic_baseline_location_on_40) // TODO: Change to app icon or wait icon
                ; // TODO: Move to strings

        progressDialog2.setTitle(getString(R.string.location_loading)); // set text for dialog
        String[] a = getResources().getStringArray(R.array.categories_for_filter);
        String[] b = getResources().getStringArray(R.array.times);

        spinner = rootView.findViewById(R.id.category_spinner_filter); // ****categories spinner****
        times_spinner = rootView.findViewById(R.id.time_spinner_filter);//****times spinner****
        search_btn = rootView.findViewById(R.id.search_btn); // *****First search Button*****
        filter_btn = rootView.findViewById(R.id.filter_btn);// *****First filter Button*****
        location_filter=rootView.findViewById(R.id.location_filter);// ****First location filter button****
        clean_btn = rootView.findViewById(R.id.clean_filter_btn); // ****clean filters button****
        close_search_btn = rootView.findViewById(R.id.close_search); // ****close search button****
        close_filter_button = rootView.findViewById(R.id.close_all_filters);// ****close filter button****
        edit_current_search = rootView.findViewById(R.id.close_current_search); // ****edit search button****
        edit_current_filters = rootView.findViewById(R.id.close_current_filter); // ****edit filter button****
        bubbleSeekBar = (BubbleSeekBar) rootView.findViewById(R.id.BubbleSeekBar); // ****SeekBar****
        manager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        current_text = rootView.findViewById(R.id.current);
        search_tv = rootView.findViewById(R.id.search_tv);
        current_search = rootView.findViewById(R.id.current_search);
        search_submit = rootView.findViewById(R.id.linear_submitsearch);
        linear_current_filter = rootView.findViewById(R.id.linear_current_filter);
        search = rootView.findViewById(R.id.linear_search);
        filters = rootView.findViewById(R.id.linear_filter);
        filter_submit = rootView.findViewById(R.id.linear_filter_submit);
        keyword = rootView.findViewById(R.id.keyword);
        keyword.setText("");
        times_spinner.setSelection(0);
        submit_filter_btn = rootView.findViewById(R.id.submit_filter);
        filter1 = rootView.findViewById(R.id.filter1);
        filter2 = rootView.findViewById(R.id.filter2);
        search_keyword = rootView.findViewById(R.id.your_search);
        submit_search_btn = rootView.findViewById(R.id.submit_serach);


        search_btn.setOnClickListener(new View.OnClickListener() { // listener for search button from menu
            @Override
            public void onClick(View v) {
                if (search.getVisibility() == View.GONE) { // if search option is closed
                    filters.setVisibility(View.GONE);  // hide filters
                    filter_submit.setVisibility(View.GONE);  // hide submit filter button
                    search.setVisibility(View.VISIBLE); // show search option
                    search_submit.setVisibility(View.VISIBLE); // show submit search button
                    current_search.setVisibility(View.GONE);
                }

            }
        });



        filter_btn.setOnClickListener(new View.OnClickListener() {  // listener for filter button from menu
            @Override
            public void onClick(View v) {
                if (linear_current_filter.getVisibility() == View.GONE) { // if filter option is closed
                    filters.setVisibility(View.VISIBLE); // show filters
                    filter_submit.setVisibility(View.VISIBLE); // hide submit filter button
                    search.setVisibility(View.GONE); // hide search option
                    search_submit.setVisibility(View.GONE);// hide submit search button
                }
            }
        });



        clean_btn.setOnClickListener(new View.OnClickListener() { // ****listener for clean filters button****
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                spinner.setSelection(0); // reset spinner
                times_spinner.setSelection(0); // reset spinner
                linear_current_filter.setVisibility(View.GONE); // hide all current result layout

            }
        });



        close_search_btn.setOnClickListener(new View.OnClickListener() { // listener for close search button layout
            @Override
            public void onClick(View v) {
                search.setVisibility(View.GONE); // hide search option
                search_submit.setVisibility(View.GONE); // hide submit search button
            }
        });



        close_filter_button.setOnClickListener(new View.OnClickListener() { // listener for close filter button layout
            @Override
            public void onClick(View v) {
                filters.setVisibility(View.GONE); // hide filter option
                filter_submit.setVisibility(View.GONE); // hide submit filter button
                linear_current_filter.setVisibility(View.GONE); // hide all current result layout
            }
        });



        location_filter.setOnClickListener(new View.OnClickListener() { // listener for get location Seekbar
            // TODO: Move to strings
            @Override
            public void onClick(View v) {
                progressDialog2.show(); //start loading dialog
                if (mUserUid==null || mUserUid.equals(""))
                {
                    if (Build.VERSION.SDK_INT >= 23) { //check permissions for gps location and get it.
                        int hasLocationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                        } else {
                            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, RecyclerViewFragment.this);
                        }
                    } else
                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, RecyclerViewFragment.this);
                }
                else
                {

                }

            }
        });



        edit_current_search.setOnClickListener(new View.OnClickListener() { // listener for edit search button - pencil
            @Override
            public void onClick(View v) {
                edit_current_search.setVisibility(View.GONE); // hide edit button
                search_keyword.setVisibility(View.GONE); //hide  the current keyword.
                current_search.setVisibility(View.GONE); // hide the text view in red for results
                search_tv.setVisibility(View.VISIBLE); // show search textview near edittext again
                keyword.setVisibility(View.VISIBLE); //show edit text again
                search_submit.setVisibility(View.VISIBLE); //show submit layout again
            }
        });


        edit_current_filters.setOnClickListener(new View.OnClickListener() { //listener for edit filters button
            @Override
            public void onClick(View v) {
                filter_submit.setVisibility(View.VISIBLE); // show filter submit button
                filters.setVisibility(View.VISIBLE); // show filters options
                linear_current_filter.setVisibility(View.GONE); // hide current filter results
            }
        });



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
        times_spinner.setAdapter(adapter2);
        times_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (times_spinner.getSelectedItem().toString()) {
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

        if (location_original.getLatitude()!=0.0 && location_original.getLongitude()!=0.0) // #case no location
            bubbleSeekBar.setVisibility(View.VISIBLE);


            bubbleSeekBar.setProgress((float) (100.0));
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(final BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        if (location_original.getLatitude()!=0.0 && location_original.getLongitude()!=0.0) // #case no location
                        {
                            showallposts(snapshot);
                            Toast.makeText(getContext(), "2) option A - no location", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            showallpostsNoLocation(snapshot);
                            Toast.makeText(getContext(), "option B - no location", Toast.LENGTH_SHORT).show();

                        }

                        Toast.makeText(getContext(), "1) option A - location Exist", Toast.LENGTH_SHORT).show();
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



        submit_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_keyword.setText(keyword.getText().toString());
                search_keyword.setVisibility(View.VISIBLE);
                current_search.setVisibility(View.VISIBLE); //%%
                keyword.setVisibility(View.GONE);
                search_tv.setVisibility(View.GONE);
                search_keyword.animate().rotation(search_keyword.getRotation() + 360).start();
                edit_current_search.setVisibility(View.VISIBLE);
                search_submit.setVisibility(View.GONE);
                if (search_keyword.getText().toString().equals(""))
                {
                    search.setVisibility(View.VISIBLE);
                    keyword.setVisibility(View.VISIBLE);
                    search_tv.setVisibility(View.VISIBLE);
                    current_search.setVisibility(View.GONE);
                    search_keyword.setVisibility(View.GONE);
                    search_submit.setVisibility(View.VISIBLE);
                    edit_current_search.setVisibility(View.GONE);

                }
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        if (location_original.getLatitude()!=0.0 && location_original.getLongitude()!=0.0) // #case no location
                        {
                            showallposts(snapshot);
                            Toast.makeText(getContext(), "2) option A - no location", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            showallpostsNoLocation(snapshot);
                            Toast.makeText(getContext(), "option B - no location", Toast.LENGTH_SHORT).show();

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
                                !(times_spinner.getItemAtPosition(0).equals(times_spinner.getSelectedItem()))) {
                            linear_current_filter.setVisibility(View.VISIBLE);
                            filter2.setText(times_spinner.getSelectedItem().toString());
                            filter2.animate().rotation(filter2.getRotation() + 360).start();
                            filter1.setVisibility(View.GONE);
                            filter2.setVisibility(View.VISIBLE);
                            edit_current_filters.setVisibility(View.VISIBLE);
                            current_text.setVisibility(View.VISIBLE);

                        } else if (!(spinner.getItemAtPosition(0).equals(spinner.getSelectedItem())) &&
                                times_spinner.getItemAtPosition(0).equals(times_spinner.getSelectedItem())) {
                            linear_current_filter.setVisibility(View.VISIBLE);
                            filter1.setText(spinner.getSelectedItem().toString());
                            filter1.animate().rotation(filter1.getRotation() + 360).start();
                            filter2.setVisibility(View.GONE);
                            filter1.setVisibility(View.VISIBLE);
                            edit_current_filters.setVisibility(View.VISIBLE);
                            current_text.setVisibility(View.VISIBLE);


                        } else if (!(spinner.getItemAtPosition(0).equals(spinner.getSelectedItem())) &&
                                !(times_spinner.getItemAtPosition(0).equals(times_spinner.getSelectedItem()))) {
                            linear_current_filter.setVisibility(View.VISIBLE);
                            filter1.setVisibility(View.VISIBLE);
                            filter2.setVisibility(View.VISIBLE);
                            current_text.setVisibility(View.VISIBLE);
                            edit_current_filters.setVisibility(View.VISIBLE);
                            filter1.setText(spinner.getSelectedItem().toString());
                            filter1.animate().rotation(filter1.getRotation() + 360).start();
                            filter2.setText(times_spinner.getSelectedItem().toString());
                            filter2.animate().rotation(filter2.getRotation() + 360).start();

                        } else if (spinner.getItemAtPosition(0).equals(spinner.getSelectedItem()) &&
                                times_spinner.getItemAtPosition(0).equals(times_spinner.getSelectedItem())) {
                            filter1.setVisibility(View.GONE);
                            filter2.setVisibility(View.GONE);
                            current_text.setVisibility(View.GONE);
                            linear_current_filter.setVisibility(View.GONE);
                            edit_current_filters.setVisibility(View.GONE);
                        }
//                        if (!keyword.getText().toString().equals("") && keyword!=null)
                        if (!keyword.getText().toString().equals(""))
                            search.setVisibility(View.VISIBLE);
//
                        if (location_original.getLatitude()!=0.0 && location_original.getLongitude()!=0.0) // #case no location
                        {
                            Toast.makeText(getContext(), "3) option A - location Exist", Toast.LENGTH_SHORT).show();
                            showallposts(snapshot);

                        }
                        else
                        {
                            showallpostsNoLocation(snapshot);
                            Toast.makeText(getContext(), "option B - no location", Toast.LENGTH_SHORT).show();

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

        refreshLayout = rootView.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        if (location_original.getLatitude()!=0.0 && location_original.getLongitude()!=0.0) // #case no location
                        {
                            showallposts(snapshot);
                            Toast.makeText(getContext(), "option A - location Exist", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            showallpostsNoLocation(snapshot);
                            Toast.makeText(getContext(), "option B - no location", Toast.LENGTH_SHORT).show();

                        }
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
        Toast.makeText(getContext(), "heyyy", Toast.LENGTH_SHORT).show();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                postList.clear();
                if (location_original.getLatitude()!=0.0 && location_original.getLongitude()!=0.0) // #case no location
                {
                    showallposts(snapshot);
                    Toast.makeText(getContext(), "CUSTOM==option A - no location", Toast.LENGTH_SHORT).show();
                    flagBack=0;
                }
                else
                {
                    showallpostsNoLocation(snapshot);
                    Toast.makeText(getContext(), "CUSTOM==option B - no location", Toast.LENGTH_SHORT).show();

                }                Collections.reverse(postList);
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

    private void showallpostsNoLocation(DataSnapshot snapshot) {
        for (DataSnapshot ds : snapshot.getChildren()) {
            Post post = ds.getValue(Post.class);
            String coordinates = post.coordinates;
            String a[] = coordinates.split(",");
            Location location2 = new Location("dummyProvider");
            location2.setLatitude(Double.parseDouble(a[0]));
            location2.setLongitude(Double.parseDouble(a[1]));
            SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
            String inputString2 = myFormat.format(date);
            try {
                Date date1 = myFormat.parse(post.getDate());
                Date date2 = myFormat.parse(inputString2);
                diff = date2.getTime() - date1.getTime();// calculate the difference
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(post.desc.toLowerCase().contains(keyword.getText().toString().toLowerCase()) &&
                    (post.getCategory().equals(spinner.getSelectedItem().toString()) ||
                            spinner.getSelectedItem().toString().equals("All Categories") ||
                            spinner.getSelectedItem().toString().equals(spinner.getItemAtPosition(0))) )// if post contain search

            {
                if (required_days != -1)
                {

                    if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                        postList.add(post);
                    }
                }
                else {
                    postList.add(post);
                }
            }
            else  if(!post.desc.toLowerCase().contains(keyword.getText().toString().toLowerCase()))
            {

            }
        }
    }

    private void nofilters(DataSnapshot snapshot) {
        for (DataSnapshot ds : snapshot.getChildren()) {
            Post post = ds.getValue(Post.class);
            String coordinates = post.coordinates;
            String a[] = coordinates.split(",");
            Location location2 = new Location("dummyProvider");
            location2.setLatitude(Double.parseDouble(a[0]));
            location2.setLongitude(Double.parseDouble(a[1]));
             postList.add(post);

        }
    }



    private void showallposts(DataSnapshot snapshot) { // show posts by filters and search
        for (DataSnapshot ds : snapshot.getChildren()) {
            Post post = ds.getValue(Post.class);
            String coordinates = post.coordinates;
            String a[] = coordinates.split(",");
            Location location2 = new Location("dummyProvider");
            location2.setLatitude(Double.parseDouble(a[0]));
            location2.setLongitude(Double.parseDouble(a[1]));
            SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
            String inputString2 = myFormat.format(date);
            try {
                Date date1 = myFormat.parse(post.getDate());
                Date date2 = myFormat.parse(inputString2);
                diff = date2.getTime() - date1.getTime();// calculate the difference
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(post.desc.toLowerCase().contains(keyword.getText().toString().toLowerCase()) &&
                    (post.getCategory().equals(spinner.getSelectedItem().toString()) ||
                            spinner.getSelectedItem().toString().equals("All Categories") ||
                            spinner.getSelectedItem().toString().equals(spinner.getItemAtPosition(0))) && (location_original.distanceTo(location2) <= bubbleSeekBar.getProgress()*1000))// if post contain search

            {
                if (required_days != -1)
                {

                    if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                        postList.add(post);
                    }
                    }
                else {
                        postList.add(post);
                    }
                }
            else  if(!post.desc.toLowerCase().contains(keyword.getText().toString().toLowerCase()))
            {

            }
            }}

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // permission function
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