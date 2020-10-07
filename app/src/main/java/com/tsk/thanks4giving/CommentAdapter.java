package com.tsk.thanks4giving;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentCardHolder> {

    private ArrayList<Comment> list;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference comments = database.getReference();

    public CommentAdapter(ArrayList<Comment> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public CommentCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);
        CommentAdapter.CommentCardHolder holder = new CommentAdapter.CommentCardHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentCardHolder holder, int position) {
        final Comment comment2 = list.get(position);
        holder.profileName.setText(comment2.getUserName());
        holder.message.setText(comment2.getText());

        /*DatabaseReference ref = comments.child("comments").child(comment2.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Comment com = snapshot.getValue(Comment.class);
                if (com != null)
                {
                    holder.profileName.setText(com.getUserName());
                    holder.message.setText(com.getText());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CommentCardHolder extends RecyclerView.ViewHolder {
        TextView profileName;
        TextView message;

        public CommentCardHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.comment_profile_name);
            message = itemView.findViewById(R.id.comment_message);
        }
    }

}
