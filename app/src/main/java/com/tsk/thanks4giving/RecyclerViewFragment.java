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
import android.widget.AutoCompleteTextView;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecyclerViewFragment extends Fragment {

    private static final String ARG_USER_UID = "userUid";
    private static final String ARG_STATUS = "itemsStatus";
    private String mUserUid;
    private int mItemsStatus;

    final String POST_FRAG = "Post Fragment";

    ArrayList<Post> postList = new ArrayList<>();
    RecyclerView recycler;
    PostAdapter adapter;

    SwipeRefreshLayout refreshLayout;
    BubbleSeekBar bubbleSeekBar;

    EditText keyword;
    TextView current_text, search_tv, current_search,current_distance;
    Spinner times_spinner;
    Button filter_btn, search_btn, clean_btn, submit_filter_btn, submit_search_btn, filter1, filter2, search_keyword, location_filter,your_search,your_distance;
    ImageButton close_filter_button, close_search_btn, edit_current_filters, edit_current_search;
    LinearLayout filters, search, filter_submit, search_submit, linear_current_filter, all_buttons_layout,linear_search;
    LovelyProgressDialog progressDialog2;
    AutoCompleteTextView categoryDropdown;
    FloatingActionButton search_floating;
    final String RECYCLER_FRAG = "Recycler View Fragment";
    private int category;
    private String time = "";
    String word="";
    int distance=-1;


    long diff;
    int flagBack = -1, LOCATION_PERMISSION_REQUEST = 2;
    int required_days=-1;
    Location location_original = new Location("dummyProvider");
    LocationManager manager; //##

    Query query;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference posts = database.getReference().child("posts");
    final DatabaseReference users = database.getReference().child("users");

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
        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        all_buttons_layout = rootView.findViewById(R.id.all_buttons_layout);
        linear_search= rootView.findViewById(R.id.linear_search);
        linear_current_filter=rootView.findViewById(R.id.linear_current_filter);
        keyword = rootView.findViewById(R.id.keyword);
        your_search=rootView.findViewById(R.id.your_search);
        current_distance=rootView.findViewById(R.id.current_distance);
        your_distance=rootView.findViewById(R.id.your_distance);
        filter1=rootView.findViewById(R.id.filter1);
        filter2=rootView.findViewById(R.id.filter2);
        search_floating = rootView.findViewById(R.id.search_floating);


        refreshLayout = rootView.findViewById(R.id.refresh);
        adapter = new PostAdapter(postList);
        recycler = rootView.findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
        if (getArguments() != null && getArguments().getInt("flag") != 2)  {

            all_buttons_layout.setVisibility(View.GONE);
            search_floating.setVisibility(View.GONE);
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshLayout.setRefreshing(false);
                }
            });
            mUserUid = getArguments().getString(ARG_USER_UID);
            mItemsStatus = getArguments().getInt(ARG_STATUS);
            query = posts.orderByChild("status").equalTo(mItemsStatus);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Post post = ds.getValue(Post.class);
                        Toast.makeText(getContext(), post.getAddress(), Toast.LENGTH_SHORT).show(); // TODO: Delete
                        if (post.getUserUid().equals(mUserUid))
                            postList.add(post);
                        adapter.notifyDataSetChanged();
                        Collections.reverse(postList);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        if (getArguments() == null || getArguments().getInt("flag") == 2) {

            if (getArguments() != null) {
//                keyword.setText(getArguments().getString("keyword"));
                word = getArguments().getString("keyword");
                time = getArguments().getString("time");
                distance = getArguments().getInt("distance");
//                category = getArguments().getInt("category");
                location_original.setLatitude(getArguments().getDouble("lat"));
                location_original.setLongitude(getArguments().getDouble("long"));

                if (word!=null && !word.equals("") &&distance>0) //together
                {
                    linear_search.setVisibility(View.VISIBLE);
                    your_search.setText(word);
                    your_search.animate().rotation(your_search.getRotation() + 360).start();

                    your_distance.setVisibility(View.VISIBLE);
                    your_distance.setText(distance/1000 +" km");
                    your_distance.animate().rotation(your_search.getRotation() + 360).start();
                }
                else if (word!=null && !word.equals("") &&distance==0) //only word
                {
                    linear_search.setVisibility(View.VISIBLE);
                    your_search.setText(word);
                    your_search.animate().rotation(your_search.getRotation() + 360).start();
                    current_distance.setVisibility(View.GONE);
                    your_distance.setVisibility(View.GONE);
                }
                else if (word==null | word.equals("") &&distance>0)
                {
                    linear_search.setVisibility(View.VISIBLE);
                    your_search.setVisibility(View.GONE);
                    current_distance.setVisibility(View.VISIBLE);
                    your_distance.setVisibility(View.VISIBLE);
                    your_distance.setText(distance/1000 +" km");
                    your_distance.animate().rotation(your_distance.getRotation() + 360).start();
                }
                else
                {
                    linear_search.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), "cat"+ category+", time "+time, Toast.LENGTH_SHORT).show();
                if (category!=0 && !time.equals("Time"))
                {
                    linear_current_filter.setVisibility(View.VISIBLE);
                    filter1.setVisibility(View.VISIBLE);
                    filter2.setVisibility(View.VISIBLE);
                    filter1.setText("cat");
                    filter2.setText(time);
                    filter1.animate().rotation(filter1.getRotation() + 360).start();
                    filter2.animate().rotation(filter2.getRotation() + 360).start();
                    Toast.makeText(getContext(), "11", Toast.LENGTH_SHORT).show();

                }
                else if (!time.equals("Time") && category==0) // only time
                {
                    linear_current_filter.setVisibility(View.VISIBLE);
                    filter1.setVisibility(View.GONE);
                    filter2.setVisibility(View.VISIBLE);
                    filter2.setText(time);
                    filter2.animate().rotation(filter1.getRotation() + 360).start();
                    Toast.makeText(getContext(), "22", Toast.LENGTH_SHORT).show();

                }

                else if (category!=0 && (time.equals("Time"))) // only category
                {
                    linear_current_filter.setVisibility(View.VISIBLE);
                    filter1.setVisibility(View.VISIBLE);
                    filter1.setText("category");
                    filter1.animate().rotation(filter1.getRotation() + 360).start();
                    filter2.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "sssdsdsdsdsd", Toast.LENGTH_SHORT).show();
                }
//                else
//                {
//                    linear_current_filter.setVisibility(View.GONE);
//
//                }





//                postList = getArguments().getParcelableArrayList("array");
                Toast.makeText(getContext(), getArguments().getString("keyword")
                        + getArguments().getString("time")
                        + getArguments().getString("category")
                        + getArguments().getDouble("lat")
                        + getArguments().getDouble("long")+" ==="+distance, Toast.LENGTH_SHORT).show();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (postList!=null)
                            postList.clear();
//                        Toast.makeText(getContext(),"thissssss = " +word, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getContext(),"thissssss time = " +time, Toast.LENGTH_SHORT).show();
                        switch (time) {
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
                                showallpostsNoFilters(snapshot, word,time);
                        Collections.reverse(postList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            search_floating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFragment(new FiltersFragment(), "");
//                    spinner.setSelection(3); // reset spinner
//                    times_spinner.setSelection(2);


                }
            });

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList.clear();
                    showallpostsNoFilters(snapshot,word,time);
                    Collections.reverse(postList);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


//
//
//            submit_search_btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    search_keyword.setText(keyword.getText().toString());
//                    search_keyword.setVisibility(View.VISIBLE);
//                    current_search.setVisibility(View.VISIBLE); //%%
//                    keyword.setVisibility(View.GONE);
//                    search_tv.setVisibility(View.GONE);
//                    search_keyword.animate().rotation(search_keyword.getRotation() + 360).start();
//                    edit_current_search.setVisibility(View.VISIBLE);
//                    search_submit.setVisibility(View.GONE);
//                    if (search_keyword.getText().toString().equals("")) {
//                        search.setVisibility(View.VISIBLE);
//                        keyword.setVisibility(View.VISIBLE);
//                        search_tv.setVisibility(View.VISIBLE);
//                        current_search.setVisibility(View.GONE);
//                        search_keyword.setVisibility(View.GONE);
//                        search_submit.setVisibility(View.VISIBLE);
//                        edit_current_search.setVisibility(View.GONE);
//                    }
//
//                    query.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            postList.clear();
//                            if (location_original.getLatitude() != 0.0 && location_original.getLongitude() != 0.0) { // #case no location
//                                showAllPosts(snapshot);
//                            } else {
//                                showallpostsNoLocation(snapshot);
//

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshLayout.setRefreshing(false);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            postList.clear();
                            showallpostsNoFilters(snapshot,word,time);
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
        }
        return rootView;
    }

    private void showallpostsNoFilters(DataSnapshot snapshot,String word,String time1) {


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
                Date date1 = myFormat.parse(post.date);
                Date date2 = myFormat.parse(inputString2);
                diff = date2.getTime() - date1.getTime();// calculate the difference
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            Toast.makeText(getContext(), ""+required_days+","+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS), Toast.LENGTH_SHORT).show(); // TODO: Delete
//            Toast.makeText(getContext(),"dis = " +location_original.distanceTo(location2), Toast.LENGTH_SHORT).show();
            if (location_original.getLatitude()!=0.0 &&location_original.getLongitude()!=0.0)
            {
                if (required_days != -1) {
                if (post.getDesc().contains(word) && TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days &&  (location_original.distanceTo(location2) <= distance))
                    postList.add(post);
            }
            else if (post.getDesc().contains(word) &&  (location_original.distanceTo(location2) <= distance))
                postList.add(post);

            }
            else
            {
                if (required_days != -1) {
                    if (post.getDesc().contains(word) && TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days )
                        postList.add(post);
                }
                else if (post.getDesc().contains(word))
                    postList.add(post);

            }


                }

        }

    private void setFragment(Fragment fragment, String FRAG) {
        // Prevent opening the same fragment twice
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        List<Fragment> list = fragmentManager.getFragments();
        // Get last fragment
        if (!list.isEmpty()) {
            Fragment topFragment = list.get(list.size() - 1);
            if (topFragment != null && topFragment.getTag().equals(FRAG)) {
                fragmentManager.popBackStack();
            }
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flContent, fragment, FRAG);
        if (!FRAG.equals(RECYCLER_FRAG)) transaction.addToBackStack(null);
        transaction.commit();
    }

}