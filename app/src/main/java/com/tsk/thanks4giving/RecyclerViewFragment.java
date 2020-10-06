package com.tsk.thanks4giving;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;

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

    private String mUserUid;
    private int mItemsStatus;

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