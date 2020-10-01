package com.tsk.thanks4giving;

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

import java.util.ArrayList;

public class RecyclerViewFragment extends Fragment {

    final String POST_FRAG = "Post Fragment";
    ArrayList<Post> postList = new ArrayList<>();
    RecyclerView recycler;
    PostAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        String path = "android.resource://com.tsk.thanks4giving/drawable/ic_home";
        for (int i = 0; i < 10; i++) {
            Post post = new Post("post", path, path, i + 100, null, null);
            postList.add(post);
        }

        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        recycler = rootView.findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(postList);
        adapter.notifyDataSetChanged();

        adapter.setListener(new PostAdapter.PostClickListener() {
            @Override
            public void onClickListener(int pos, View v) {
                FragmentManager fragmentManager = getFragmentManager();
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