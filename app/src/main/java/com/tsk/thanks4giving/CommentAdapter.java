package com.tsk.thanks4giving;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentCardHolder> {

    private ArrayList<Comment> list;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference users = database.getReference().child("users");

    public CommentAdapter(ArrayList<Comment> list) {
        this.list = list;
    }

    public class CommentCardHolder extends RecyclerView.ViewHolder {
        TextView user_name, message, date;
        CircleImageView profile_photo_iv;

        public CommentCardHolder(@NonNull View itemView) {
            super(itemView);
            profile_photo_iv = itemView.findViewById(R.id.comment_profile_img);
            user_name = itemView.findViewById(R.id.comment_profile_name);
            message = itemView.findViewById(R.id.comment_message);
            date = itemView.findViewById(R.id.comment_date);

        }
    }

    @NonNull
    @Override
    public CommentCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);
        return new CommentCardHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentCardHolder holder, int position) {
        final Context context = holder.itemView.getContext();
        final Comment comment = list.get(position);
        users.child(comment.userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(context)
                        .load(user.profilePhoto)
                        .centerCrop()
                        .into(holder.profile_photo_iv);
                holder.user_name.setText(user.name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.message.setText(comment.text);
        holder.date.setText(comment.date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
