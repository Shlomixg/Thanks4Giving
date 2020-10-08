package com.tsk.thanks4giving;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
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
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecyclerViewFragment extends Fragment {

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
    Spinner spinner,timer_spinner;
    Button filter_btn,search_btn,clean_btn,submit_filter_btn,submit_search_btn;
    ImageButton close_btn,close_search_btn;
    LinearLayout filters,search,filter_submit,search_submit;
    long diff;
    int required_days;

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
        search_btn=rootView.findViewById(R.id.search_btn);
        search_submit=rootView.findViewById(R.id.linear_submitsearch);
        search=rootView.findViewById(R.id.linear_search);
        filters=rootView.findViewById(R.id.linear_filter);
        filter_submit=rootView.findViewById(R.id.linear_filter_submit);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.setVisibility(View.GONE);
                filter_submit.setVisibility(View.GONE);
            }
        });
        clean_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                spinner.setSelection(0);
                timer_spinner.setSelection(0);

//                Toast.makeText(getContext(), "Days: "+daysBetween, Toast.LENGTH_SHORT).show();

            }
        });
//        linearLayout_filters=rootView.findViewById(R.id.all_filters);
        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.setVisibility(View.VISIBLE);
                filter_submit.setVisibility(View.VISIBLE);
                search.setVisibility(View.GONE);
                search_submit.setVisibility(View.GONE);

            }
        });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.setVisibility(View.GONE);
                filter_submit.setVisibility(View.GONE);
                search.setVisibility(View.VISIBLE);
                search_submit.setVisibility(View.VISIBLE);

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
                switch (timer_spinner.getSelectedItem().toString())
                {
                    case "Today":
                        required_days=1;
                        break ;
                    case "Last 3 days":
                        required_days=3;
                        break ;
                    case "Last week":
                        required_days=7;
                        break ;
                    case "All posts":
                        required_days=-1;
                        break ;
                    case "Time":
                        required_days=-1;
                        break ;
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timer_spinner.setSelection(0);




        submit_filter_btn=rootView.findViewById(R.id.submit_filter);
        submit_search_btn=rootView.findViewById(R.id.submit_serach);
        submit_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post pos = ds.getValue(Post.class);
                            Location location = new Location("dummyProvider");
                            location.setLatitude(32.0627896);
                            location.setLongitude(34.7714756);
                            String coordinates = pos.getCoordinates();
                            String a[] = coordinates.split(",");
                            Location location2 = new Location("dummyProvider");
                            location2.setLatitude(Double.parseDouble(a[0]));
                            location2.setLongitude(Double.parseDouble(a[1]));
                            SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = new Date();
                            String inputString1 = pos.getDate();
                            String inputString2 = myFormat.format(date);
                            System.out.println(inputString1);
                            System.out.println(inputString2);
                            try {
                                Date date1 = myFormat.parse(inputString1);
                                Date date2 = myFormat.parse(inputString2);
                                diff = date2.getTime() - date1.getTime();
                                Toast.makeText(getContext(), "Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS), Toast.LENGTH_SHORT).show();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getContext(), "rew Days: " + required_days, Toast.LENGTH_SHORT).show();


                            if (location.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
                                if (pos.getDesc().contains(keyword.getText().toString()) && pos.getCategory().equals(spinner.getSelectedItem())) {
                                    if (required_days != -1) {

                                        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                                            postList.add(pos);

                                        }
                                    } else {
                                        postList.add(pos);

                                    }
                                } else if (pos.getDesc().contains(keyword.getText().toString()) && spinner.getSelectedItem().equals("All Categories")) {
                                    if (required_days != -1) {

                                        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                                            postList.add(pos);

                                        }
                                    } else {
                                        postList.add(pos);

                                    }


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

        keyword=rootView.findViewById(R.id.keyword);
        submit_filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post pos = ds.getValue(Post.class);
                            Location location = new Location("dummyProvider");
                            location.setLatitude(32.0627896);
                            location.setLongitude(34.7714756);
                            String coordinates = pos.getCoordinates();
                            String a[] = coordinates.split(",");
                            Location location2 = new Location("dummyProvider");
                            location2.setLatitude(Double.parseDouble(a[0]));
                            location2.setLongitude(Double.parseDouble(a[1]));
                            SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = new Date();
                            String inputString1 = pos.getDate();
                            String inputString2 = myFormat.format(date);
                            System.out.println(inputString1);
                            System.out.println(inputString2);
                            try {
                                Date date1 = myFormat.parse(inputString1);
                                Date date2 = myFormat.parse(inputString2);
                                diff = date2.getTime() - date1.getTime();
                                Toast.makeText(getContext(), "Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS), Toast.LENGTH_SHORT).show();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getContext(), "rew Days: " + required_days, Toast.LENGTH_SHORT).show();


                            if (location.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
                                if (pos.getDesc().contains(keyword.getText().toString()) && pos.getCategory().equals(spinner.getSelectedItem())) {
                                    if (required_days != -1) {

                                        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                                            postList.add(pos);

                                        }
                                    } else {
                                        postList.add(pos);

                                    }
                                } else if (pos.getDesc().contains(keyword.getText().toString()) && (spinner.getSelectedItem().equals("All Categories") || spinner.getItemAtPosition(0).equals(spinner.getSelectedItem())))
                            {
                                    if (required_days != -1) {

                                        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= required_days) {
                                            postList.add(pos);

                                        }
                                    } else {
                                        postList.add(pos);

                                    }


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





        bubbleSeekBar = (BubbleSeekBar) rootView.findViewById(R.id.BubbleSeekBar);
        bubbleSeekBar.setProgress((float) (100.0));
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(final BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post pos = ds.getValue(Post.class);
                            Location location = new Location("dummyProvider");
                            location.setLatitude(32.0627896);
                            location.setLongitude(34.7714756);
                            String coordinates = pos.getCoordinates();
                            String a[] = coordinates.split(",");
                            Location location2 = new Location("dummyProvider");
                            location2.setLatitude(Double.parseDouble(a[0]));
                            location2.setLongitude(Double.parseDouble(a[1]));
                            if (location.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
                                postList.add(pos);

//                    {
//                        holder.edit_btn.setVisibility(View.VISIBLE);
//                    }
                                                   }
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
                        filterLocation(snapshot);
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
                filterLocation(snapshot);
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
                String postId = postList.get(pos).getPostID();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d("ddd", "event reached fragment");
        //TODO refresh posts list with new location
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
            Post pos = ds.getValue(Post.class);
            Location location = new Location("dummyProvider");
            location.setLatitude(32.0627896);
            location.setLongitude(34.7714756);
            String coordinates = pos.getCoordinates();
            String a[] = coordinates.split(",");
            Location location2 = new Location("dummyProvider");
            location2.setLatitude(Double.parseDouble(a[0]));
            location2.setLongitude(Double.parseDouble(a[1]));
            if (location.distanceTo(location2) <= bubbleSeekBar.getProgress() * 1000)
                postList.add(pos);
        }
    }
}