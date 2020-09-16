package com.tsk.thanks4giving;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostCardHolder> {

    ImageView postImage;
    ImageView profileImage;
    ImageButton like;
    ImageButton comment;
    ImageButton watch;
    private PostClickListener listener;

    public void setListener(PostClickListener listener){this.listener=listener;}

    interface PostClickListener{
        void onClickListener(int pos, View v);
        void onLongClickListener(int pos, View v);
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


    @NonNull
    @Override
    public PostAdapter.PostCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PostCardHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class PostCardHolder extends RecyclerView.ViewHolder {

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
                    if(listener!=null)
                        listener.onClickListener(getAdapterPosition(),v);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(listener!=null)
                        listener.onLongClickListener(getAdapterPosition(),v);
                    return false;
                }
            });
        }
    }
}
