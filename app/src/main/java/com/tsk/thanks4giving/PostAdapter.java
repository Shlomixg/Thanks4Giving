package com.tsk.thanks4giving;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    final DatabaseReference likes = database.getReference().child("likes");
    final DatabaseReference comments = database.getReference().child("comments");
    final DatabaseReference follows = database.getReference().child("follows");
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Drawable like, like_fill, follow, follow_fill;

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
        MaterialButton like_btn, comment_btn, follow_btn, edit_btn;
        TextView itemTitleTV, itemCategoryTV, itemDescTV, userNameTV, itemDateTV;

        public PostCardHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_item_img);
            profileImage = itemView.findViewById(R.id.post_profile_img);
            edit_btn = itemView.findViewById(R.id.post_edit_btn);
            userNameTV = itemView.findViewById(R.id.post_user_name);
            itemTitleTV = itemView.findViewById(R.id.post_item_title);
            itemCategoryTV = itemView.findViewById(R.id.post_item_category);
            itemDescTV = itemView.findViewById(R.id.post_item_desc);
            itemDateTV = itemView.findViewById(R.id.post_date);
            like_btn = itemView.findViewById(R.id.post_like_btn);
            comment_btn = itemView.findViewById(R.id.post_comment_btn);
            follow_btn = itemView.findViewById(R.id.post_follow_btn);

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
    public void onBindViewHolder(@NonNull final PostCardHolder holder, final int position) {
        final Context context = holder.itemView.getContext();

        like = ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_like, null);
        like_fill = ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_like_fill, null);
        follow = ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_bookmark, null);
        follow_fill = ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_bookmark_fill, null);

        post = list.get(position);
        final String postID = post.postID, userID = post.userUid;

        users.child(post.userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.profilePhoto != null) {
                        Glide.with(context).load(user.profilePhoto).centerCrop().into(holder.profileImage);
                    }
                    // TODO: Remove else if after fixing the signup upload
                    else if (user.gender.equals("Female")) {
                        Glide.with(context).load(R.drawable.profile_woman).centerCrop().into(holder.profileImage);
                    } else {
                        Glide.with(context).load(R.drawable.profile_man).centerCrop().into(holder.profileImage);
                    }
                    if (currentUser != null && user.uid.equals(currentUser.getUid()))
                        holder.edit_btn.setVisibility(View.VISIBLE);
                    holder.userNameTV.setText(user.name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Glide.with(context)
                .load(post.postImage)
                .fitCenter()
                .into(holder.postImage);

        holder.itemTitleTV.setText(post.title);
        holder.itemCategoryTV.setText(post.category);
        holder.itemDescTV.setText(post.desc);
        holder.itemDateTV.setText(post.date);

        likes.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = (int) snapshot.getChildrenCount();
                holder.like_btn.setText("" + size);
                if (currentUser != null && snapshot.hasChild(currentUser.getUid()))

                    holder.like_btn.setIcon(like_fill);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        comments.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = (int) snapshot.getChildrenCount();
                holder.comment_btn.setText("" + size);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Bundle bundle = new Bundle();
                bundle.putString("userUid", userID);
                ProfileFragment fragment = new ProfileFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, PROFILE_FRAG).addToBackStack(null).commit();
            }
        });

        holder.edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Bundle bundle = new Bundle();
                bundle.putString("postId", postID);
                EditPostFragment fragment = new EditPostFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, PROFILE_FRAG).addToBackStack(null).commit();
            }
        });


        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (currentUser != null) {
                    final String currUserUid = currentUser.getUid(),
                            currUserName = currentUser.getDisplayName();
                    likes.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(currUserUid)) {
                                likes.child(postID).child(currUserUid).removeValue();
                                holder.like_btn.setIcon(like);
                            } else {
                                likes.child(postID).child(currUserUid).setValue(currUserName);
                                holder.like_btn.setIcon(like_fill);
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

        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

            }
        });

        holder.follow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (currentUser != null) {
                    final String currUserUid = currentUser.getUid(),
                            currUserName = currentUser.getDisplayName();
                    follows.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(currUserUid)) {
                                follows.child(postID).child(currUserUid).removeValue();
                                holder.follow_btn.setIcon(follow);
                            } else {
                                follows.child(postID).child(currUserUid).setValue(currUserName);
                                holder.follow_btn.setIcon(follow_fill);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // TODO: Error handling
                        }
                    });
                } else {
                    Log.d("Log Follow", "Not logged in!");
                    Snackbar.make(v, R.string.must_be_logged, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}