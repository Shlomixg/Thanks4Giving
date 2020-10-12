package com.tsk.thanks4giving;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.androidribbon.RibbonLayout;
import com.skydoves.androidribbon.RibbonView;
import com.skydoves.androidribbon.ShimmerRibbonView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostCardHolder> {

    private List<Post> postList;
    private PostClickListener listener;

    final String PROFILE_FRAG = "Profile Fragment";
    final String NEW_POST_FRAG = "New Post Fragment";
    int category;

    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference users = database.getReference().child("users");
    final DatabaseReference posts = database.getReference().child("posts");
    final DatabaseReference comments = database.getReference().child("comments");
    final DatabaseReference likes = database.getReference().child("likes");
    Drawable like, like_fill;

    interface PostClickListener {
        void onClickListener(int pos, View v);
        void onLongClickListener(int pos, View v);
    }

    public void setListener(PostClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    public class PostCardHolder extends RecyclerView.ViewHolder {

        ImageView post_image_view;
        CircleImageView profile_photo_civ;
        TextView itemTitleTV, itemCategoryTV, itemDescTV, itemDateTV, userNameTV;
        MaterialButton like_btn, comment_btn, ribbon_btn, edit_btn;
        LinearLayout ribbonBtnWrapper;
        RibbonLayout ribbonLayout;

        public PostCardHolder(@NonNull View itemView) {
            super(itemView);
            post_image_view = (ImageView) itemView.findViewById(R.id.post_item_img);
            profile_photo_civ = (CircleImageView) itemView.findViewById(R.id.post_profile_img);
            itemTitleTV = (TextView) itemView.findViewById(R.id.post_item_title);
            itemCategoryTV = (TextView) itemView.findViewById(R.id.post_item_category);
            itemDescTV = (TextView) itemView.findViewById(R.id.post_item_desc);
            itemDateTV = (TextView) itemView.findViewById(R.id.post_date);
            userNameTV = (TextView) itemView.findViewById(R.id.post_user_name);
            like_btn = (MaterialButton) itemView.findViewById(R.id.post_like_btn);
            comment_btn = (MaterialButton) itemView.findViewById(R.id.post_comment_btn);
            ribbon_btn = (MaterialButton) itemView.findViewById(R.id.ribbon_btn);
            edit_btn = (MaterialButton) itemView.findViewById(R.id.post_edit_btn);
            ribbonBtnWrapper = (LinearLayout) itemView.findViewById(R.id.ribbon_wrapper);
            ribbonLayout = (RibbonLayout) itemView.findViewById(R.id.post_ribbon_layout);

            like = ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.ic_like, null);
            like_fill = ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.ic_like_fill, null);

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
        holder.setIsRecyclable(false);

        final Context context = holder.itemView.getContext();
        final Post post = postList.get(holder.getAdapterPosition());

        final ShimmerRibbonView ribbonView = new ShimmerRibbonView.Builder(context)
                .setRibbonView(new RibbonView.Builder(context)
                        .setText(context.getResources().getString(R.string.ribbon_text))
                        .setTextColor(context.getColor(R.color.colorWhite))
                        .setTextSize(18)
                        .setPaddingTop(21)
                        .setPaddingBottom(21)
                        .setPaddingLeft(250)
                        .setPaddingRight(250)
                        .setRibbonBackgroundColor(context.getColor(R.color.quantum_googred))
                        .build())
                .setShimmer(new Shimmer.AlphaHighlightBuilder()
                        .setBaseAlpha(1.0f)
                        .setHighlightAlpha(0.85f)
                        .setRepeatDelay(1200)
                        .setDuration(1200)
                        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                        .build()
                ).build();

        if (post.status == 0) {
            holder.ribbonLayout.setRibbonBottom(ribbonView);
        }

        users.child(post.userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.profilePhoto != null) {
                        Glide.with(context).load(user.profilePhoto).centerCrop().into(holder.profile_photo_civ);
                    }
                    holder.userNameTV.setText(user.name);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Glide.with(context).load(post.postImage).fitCenter().into(holder.post_image_view);
        if (currentUser != null && post.userUid.equals(currentUser.getUid())) {
            holder.edit_btn.setVisibility(View.VISIBLE);
            if (post.status == 1) {
                holder.ribbonBtnWrapper.setVisibility(View.VISIBLE);
            } else if (post.status == 0) {
                holder.ribbonBtnWrapper.setVisibility(View.GONE);
            }
        }

        final String[] categories = context.getResources().getStringArray(R.array.categories);
        category = post.category;

        holder.itemTitleTV.setText(post.title);
        holder.itemCategoryTV.setText(categories[category]);
        holder.itemDescTV.setText(post.desc);
        holder.itemDateTV.setText(post.date);

        likes.child(post.postID).addValueEventListener(new ValueEventListener() {
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

        comments.child(post.postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = (int) snapshot.getChildrenCount();
                holder.comment_btn.setText("" + size);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.profile_photo_civ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Bundle bundle = new Bundle();
                bundle.putString("userUid", post.userUid);
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
                bundle.putString("postID", post.postID);
                NewPostFragment fragment = new NewPostFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, NEW_POST_FRAG).addToBackStack(null).commit();
            }
        });

        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (currentUser != null) {
                    final String currUserUid = currentUser.getUid(),
                            currUserName = currentUser.getDisplayName();
                    likes.child(post.postID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(currUserUid)) {
                                likes.child(post.postID).child(currUserUid).removeValue();
                                holder.like_btn.setIcon(like);
                            } else {
                                likes.child(post.postID).child(currUserUid).setValue(currUserName);
                                holder.like_btn.setIcon(like_fill);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else {
                    Snackbar.make(v, R.string.must_be_logged, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        holder.ribbon_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posts.child(post.postID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        if (post != null && post.status == 1) {
                            holder.ribbonBtnWrapper.setVisibility(View.GONE);
                            posts.child(post.postID).child("status").setValue(0);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}