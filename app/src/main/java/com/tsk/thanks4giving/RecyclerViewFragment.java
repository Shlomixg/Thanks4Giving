package com.tsk.thanks4giving;

import android.app.ProgressDialog;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class RecyclerViewFragment extends Fragment {

    final String POST_FRAG = "Post Fragment";
    ArrayList<Post> postList = new ArrayList<>();
    RecyclerView recycler;
    PostAdapter adapter;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference posts = database.getReference().child("posts");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        String path = "android.resource://com.tsk.thanks4giving/drawable/ic_home"; //TODO ???

        final ProgressDialog progressDialog = new ProgressDialog(getActivity().findViewById(android.R.id.content).getContext());
        progressDialog.setTitle(getString(R.string.loading));

        posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post pos = ds.getValue(Post.class);
//                    Post pos = new Post(ds.child("posterToken").getValue(String.class)
//                            ,ds.child("postImage").getValue(String.class)
//                            ,ds.child("profileImage").getValue(String.class)
//                            ,ds.child("likes").getValue(Integer.class)
//                            ,ds.child("comments").getValue(ArrayList.class)
//                            ,ds.child("watching").getValue(ArrayList.class));
                    postList.add(pos);
                }
                // adapter=new PostAdapter(postList);
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
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.flContent, new PostFragment(), POST_FRAG).addToBackStack(null).commit();
            }
            @Override
            public void onLongClickListener(int pos, View v) {
            }
        });

        recycler.setAdapter(adapter);
        return rootView;
    }
}