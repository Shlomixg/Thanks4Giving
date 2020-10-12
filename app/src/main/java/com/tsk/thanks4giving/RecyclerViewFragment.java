package com.tsk.thanks4giving;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    FloatingActionButton search_fab;
    final String RECYCLER_FRAG = "Recycler View Fragment";
    String word = "";
    int distance = -1, category = -1;

    Location location_original = new Location("dummyProvider");

    Query query;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference posts = database.getReference().child("posts");

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
        refreshLayout = rootView.findViewById(R.id.refresh);
        recycler = rootView.findViewById(R.id.recycler);
        search_fab = rootView.findViewById(R.id.search_floating);

        adapter = new PostAdapter(postList);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        // Show all posts
        if (getArguments() == null) {
            loadPosts();

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadPosts();
                    refreshLayout.setRefreshing(false);
                }
            });

        }
        // Show posts of selected user
        else if (getArguments().getInt("flag") != 2) {
            search_fab.setVisibility(View.GONE);

            loadUserPosts();

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshLayout.setRefreshing(false);
                    loadUserPosts();
                }
            });
        }
        // show filtered posts
        else if (getArguments().getInt("flag") == 2) {
            word = getArguments().getString("keyword");
            distance = getArguments().getInt("distance");
            category = getArguments().getInt("category");
            location_original.setLatitude(getArguments().getDouble("lat"));
            location_original.setLongitude(getArguments().getDouble("long"));

            filterPosts();

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshLayout.setRefreshing(false);
                    filterPosts();
                }
            });
        }

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

        search_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new FiltersFragment(), "Filters Fragment");
            }
        });

        return rootView;
    }

    private void loadUserPosts() {
        mUserUid = getArguments().getString(ARG_USER_UID);
        mItemsStatus = getArguments().getInt(ARG_STATUS);
        query = posts.orderByChild("status").equalTo(mItemsStatus);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                adapter.notifyDataSetChanged();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null && post.userUid.equals(mUserUid))
                        postList.add(post);
                }
                Collections.reverse(postList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void loadPosts() {
        posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                adapter.notifyDataSetChanged();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) postList.add(post);
                }
                Collections.reverse(postList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void filterPosts() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                adapter.notifyDataSetChanged();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) {
                        String coordinates = post.coordinates;
                        String a[] = coordinates.split(",");
                        Location location2 = new Location("dummyProvider");
                        location2.setLatitude(Double.parseDouble(a[0]));
                        location2.setLongitude(Double.parseDouble(a[1]));
                        word = word.toLowerCase();

                        String desc = post.desc.toLowerCase();
                        String title = post.title.toLowerCase();

                        Log.d("category", "Category: " + category);
                        Log.d("category", "Post Category: " + post.category);

                        // if post contain search
                        if (desc.contains(word) || title.contains(word)) {
                            if (post.category == category || category == -1) {
                                if (location_original.getLatitude() != 0.0 && location_original.getLongitude() != 0.0) {
                                    if (location_original.distanceTo(location2) <= distance)
                                        postList.add(post);
                                } else
                                    postList.add(post);
                            }
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