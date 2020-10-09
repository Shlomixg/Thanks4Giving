package com.tsk.thanks4giving;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostFragment extends Fragment {

    ImageView post_image_view;
    TextView comment_date_tv, username_tv, post_date_tv, title_tv, category_tv, desc_tv;
    MaterialButton share_btn, edit_btn, like_btn, comment_btn, follow_btn;
    EditText comment_et;
    Button send_comment_btn;
    CircleImageView user_profile_photo_civ;
    Location location;
    Geocoder geocoder;

    RecyclerView commentsRecycler;
    CommentAdapter adapter;
    ArrayList<Comment> commentList = new ArrayList<>();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference posts = database.getReference().child("posts");
    final DatabaseReference users = database.getReference().child("users");
    final DatabaseReference comments = database.getReference().child("comments");
    final DatabaseReference likes = database.getReference().child("likes");
    final DatabaseReference follows = database.getReference().child("follows");

    String postID;
    final String SK = "AAAAV2IEwl0:APA91bGzC8ukmt6UCf6kWMg4XzoHl9RthEfzBWrhv0HGOjEHrXVr6QwsbEgdXOC2Bb79AJ-P4v4Zh0eWiqPUdamh2P83EhEFymkv3cIA-_iQ7lFSdHNlL4n11oqivy-ahWphe-ANbAYl";

    final String[] topic = new String[1];
    final String PROFILE_FRAG = "Profile Fragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        post_image_view = rootView.findViewById(R.id.post_item_img);
        title_tv = rootView.findViewById(R.id.post_item_title);
        category_tv = rootView.findViewById(R.id.post_item_category);
        desc_tv = rootView.findViewById(R.id.post_item_desc);
        username_tv = rootView.findViewById(R.id.post_user_name);
        user_profile_photo_civ = rootView.findViewById(R.id.post_frag_profile_img);
        post_date_tv = rootView.findViewById(R.id.post_date);
        edit_btn = rootView.findViewById(R.id.post_edit_btn);

        share_btn = rootView.findViewById(R.id.post_share_btn);
        like_btn = rootView.findViewById(R.id.post_like_btn);
        comment_btn = rootView.findViewById(R.id.post_comment_btn);
        follow_btn = rootView.findViewById(R.id.post_follow_btn);

        commentsRecycler = rootView.findViewById(R.id.post_comments_recycler);
        comment_et = rootView.findViewById(R.id.post_comment_et);
        comment_date_tv = rootView.findViewById(R.id.comment_date);
        send_comment_btn = rootView.findViewById(R.id.post_add_comment_btn);

        ImageButton waze = rootView.findViewById(R.id.waze);
        ImageButton googleMaps = rootView.findViewById(R.id.googlemaps);

        if (getArguments() != null) {
            Bundle bundle = this.getArguments();
            postID = bundle.getString("PostId");
        } else {
            // TODO: Error handling
        }

        final String[] postImageUri = new String[1];

        posts.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    Glide.with(PostFragment.this)
                            .load(post.postImage)
                            .centerCrop()
                            .into(post_image_view);
                    if (currentUser != null && currentUser.getUid().equals(post.userUid))
                        edit_btn.setVisibility(View.VISIBLE);
                    String coordinates = post.coordinates;
                    String[] a = coordinates.split(",");
                    location = new Location("dummyProvider");
                    location.setLatitude(Double.parseDouble(a[0]));
                    location.setLongitude(Double.parseDouble(a[1]));
                    title_tv.setText("Title");
                    desc_tv.setText(post.desc);
                    post_date_tv.setText(post.date);
                    postImageUri[0] = post.postImage;
                    // TODO: how to move out?
                    users.child(post.userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                if (user.profilePhoto != null) {
                                    Glide.with(PostFragment.this).load(user.profilePhoto).centerCrop().into(user_profile_photo_civ);
                                } else {
                                    if (user.gender.equals("Female")) {
                                        Glide.with(PostFragment.this).load(R.drawable.profile_woman).centerCrop().into(user_profile_photo_civ);
                                    } else {
                                        Glide.with(PostFragment.this).load(R.drawable.profile_man).centerCrop().into(user_profile_photo_civ);
                                    }
                                }
                                username_tv.setText(user.name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            } // TODO: error handling
        });

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("postId", postID);
                EditPostFragment fragment = new EditPostFragment();
                fragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.flContent, fragment, PROFILE_FRAG).addToBackStack(null).commit();
            }
        });

        send_comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = comment_et.getText().toString();
                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.must_be_logged), Snackbar.LENGTH_SHORT).show();
                else if (text.isEmpty())
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.no_empty_comment), Snackbar.LENGTH_SHORT).show();
                else {
                    String uid = mAuth.getCurrentUser().getUid();
                    String userName = mAuth.getCurrentUser().getDisplayName();
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    Comment newComment = new Comment(uid, userName, text, format.format(date));
                    comments.child(postID).push().setValue(newComment);
                    comment_et.setText("");

                    // send notification to post owner
                    String textToSend = getString(R.string.new_comment) + userName;
                    posts.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String uid = snapshot.child("userUid").getValue(String.class);
                            topic[0] = "commentNotif" + uid;
                            Log.d("fcm", "Post: " + topic[0]);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    final JSONObject rootObject = new JSONObject();
                    try {
                        rootObject.put("to", "/topics/" + topic[0]);
                        rootObject.put("data", new JSONObject().put("message", textToSend));
                        String url = "https://fcm.googleapis.com/fcm/send";

                        RequestQueue queue = Volley.newRequestQueue(getContext());
                        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("fcm", "response:" + response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("fcm", "error:" + error.getMessage());
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=" + SK);
                                return headers;
                            }

                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                return rootObject.toString().getBytes();
                            }
                        };
                        queue.add(request);
                        queue.start();
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        comments.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Comment comment = snap.getValue(Comment.class);
                    commentList.add(comment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(commentList);
        commentsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        post_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openView(postImageUri[0]);
            }
        });

        googleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    coordinatesToMaps();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        waze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coordinatesToWaze();
            }
        });


        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("image/*");
                Bitmap bitmap = loadBitmapFromView(post_image_view, post_image_view.getWidth(), post_image_view.getHeight()); // CREATE BITMAP
                sendIntent.putExtra(Intent.EXTRA_STREAM, SaveImage(bitmap));
                sendIntent.putExtra(Intent.EXTRA_TEXT, "#Thank4Giving\nIt's Free\nProduct:" + desc_tv.getText().toString() + "\nFind this Item in our Thank4Giving App and contact the Item owner.\nLets install our app from play store or click this link www.one.co.il");
                startActivity(sendIntent);
            }
        });
        return rootView;
    }

    private void openView(String link) {
        ArrayList<String> temp = new ArrayList();
        temp.add(link);
        new StfalconImageViewer.Builder<String>(getContext(), temp, new ImageLoader<String>() {
            @Override
            public void loadImage(ImageView imageView, String image) {
                Glide.with(getContext()).load(image).into(imageView);
            }
        }).withTransitionFrom(post_image_view).withHiddenStatusBar(true).show();
    }

    private void coordinatesToMaps() throws IOException {
        posts.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null && post.locationMethod.equals("GPS")) {
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final Address bestAddress = addresses.get(0);
                    String url = "https://www.google.com/maps/search/?api=1&query=" + bestAddress.getThoroughfare() + "," + bestAddress.getFeatureName() + "," + bestAddress.getLocality() + "," + bestAddress.getCountryName();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    String url = "https://www.google.com/maps/search/?api=1&query=" + post.address;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public Uri SaveImage(Bitmap finalBitmap) {
        Random r = new Random();
        int low = 10, high = 1000000;
        int result = r.nextInt(high - low) + low;
        File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), result + ".jpg"); //eran
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    private void coordinatesToWaze() {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String url = "https://www.waze.com/ul?ll=" + latitude + "%2C" + longitude + "&navigate=yes";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

}