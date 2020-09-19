package com.tsk.thanks4giving;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostCardHolder> {

    private ArrayList<Post> list;
    private PostClickListener listener;

    interface PostClickListener {
        void onClickListener(int pos, View v);
        void onLongClickListener(int pos, View v);
    }

    public void setListener(PostClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(ArrayList<Post> postList) {
        this.list=postList;
    }

    public class PostCardHolder extends RecyclerView.ViewHolder {

        ImageView postImage;
        ImageView profileImage;
        Button like;
        Button comment;
        Button watch;

        public PostCardHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_img);
            profileImage = itemView.findViewById(R.id.profile_img);
            like = itemView.findViewById(R.id.like_img);
            comment = itemView.findViewById(R.id.comment_btn);
            watch = itemView.findViewById(R.id.watch_btn);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onClickListener(getAdapterPosition(), v);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null)
                        listener.onLongClickListener(getAdapterPosition(), v);
                    return false;
                }
            });
        }
    }

    @NonNull
    @Override
    public PostCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout,parent,false);
        PostCardHolder holder = new PostCardHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostCardHolder holder, int position) {
        Post post = list.get(position);
        holder.postImage.setImageResource(R.drawable.background_round_padded);
        holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        holder.like.setText(post.getLikes());
        /*holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void onItemMove(ArrayList<Post> list, int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(list, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

}