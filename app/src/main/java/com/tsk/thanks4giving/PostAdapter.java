package com.tsk.thanks4giving;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsk.thanks4giving.R;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostCardHolder> {

    private ArrayList<Post> list;
    private PostClickListener listener;
    final String PROFILE_FRAG = "Profile Fragment";
    Post post;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference users = database.getReference().child("users");
    final DatabaseReference postLikes = database.getReference().child("likes");
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Drawable like, like_fill;

    interface PostClickListener {
        void onClickListener(int pos, View v);

        void onLongClickListener(int pos, View v);
    }

    public void setListener(PostClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(ArrayList<Post> postList) {
        this.list = postList;
    }

    public class PostCardHolder extends RecyclerView.ViewHolder {

        ImageView postImage;
        CircleImageView profileImage;
        MaterialButton like, watch;
        TextView likeText;


        public PostCardHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_img);
            profileImage = itemView.findViewById(R.id.profile_img);
            like = itemView.findViewById(R.id.like_img);
            likeText = itemView.findViewById(R.id.like_text);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);
        return new PostCardHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostCardHolder holder, int position) {
        post = list.get(position);
        like = ResourcesCompat.getDrawable(holder.like.getResources(), R.drawable.ic_like, null);
        like_fill = ResourcesCompat.getDrawable(holder.like.getResources(), R.drawable.ic_like_fill, null);
        final DatabaseReference postRef = users.child(post.userUid);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.profilePhoto != null) {
                        Glide.with(holder.itemView.getContext()).load(user.profilePhoto).centerCrop().into(holder.profileImage);
                    } else if (user.gender.equals("Female")) {
                        Glide.with(holder.itemView.getContext()).load(R.drawable.profile_woman).centerCrop().into(holder.profileImage);
                    } else {
                        Glide.with(holder.itemView.getContext()).load(R.drawable.profile_man).centerCrop().into(holder.profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Glide.with(holder.itemView.getContext())
                .load(post.getPostImage())
                .centerCrop()
                .into(holder.postImage);

        postLikes.child(post.postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO: Improve - show even to not logged users
                int size = (int) snapshot.getChildrenCount();
                holder.likeText.setText("" + size);
                if (currentUser != null && snapshot.hasChild(currentUser.getUid()))
                    holder.like.setIcon(like_fill);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (currentUser != null) {
                    postLikes.child(post.postID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userUid = currentUser.getUid();
                            String userName = currentUser.getDisplayName();
                            if (snapshot.hasChild(userUid)) {
                                postLikes.child(post.postID).child(userUid).removeValue();
                                holder.like.setIcon(like);
                            } else {
                                postLikes.child(post.postID).child(userUid).setValue(userName);
                                holder.like.setIcon(like_fill);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // TODO: Error handling
                        }
                    });
                } else {
                    Log.d("Log like", "Not logged in!");
                    Snackbar.make(v, R.string.must_be_logged, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        holder.watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add watch to get notifications
            }
        });

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Bundle bundle = new Bundle();
                bundle.putString("userUid", post.userUid);
                ProfileFragment fragment = new ProfileFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, PROFILE_FRAG).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}