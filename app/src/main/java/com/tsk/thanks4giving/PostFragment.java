package com.tsk.thanks4giving;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostFragment extends Fragment {

    TextView description;
    ImageView postImage;
    RecyclerView commentsRecycler;
    Button commentBtn;
    EditText comment;
    Location location;
    ImageView imageView;
    CommentAdapter adapter;
    Geocoder geocoder; //##
    String data;

    //String postID;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<Comment> commentList = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference comments = database.getReference().child("comments");
    final DatabaseReference posts = database.getReference().child("posts");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        description = rootView.findViewById(R.id.post_title);
        postImage = rootView.findViewById(R.id.BigPostImage);
        commentsRecycler = rootView.findViewById(R.id.post_comments_recycler);
        comment = rootView.findViewById(R.id.post_comment_et);
        commentBtn = rootView.findViewById(R.id.post_add_comment_btn);
        Bundle bundle = this.getArguments();
        data = bundle.getString("PostId");

        posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post pos = ds.getValue(Post.class);
                    if (pos.getPostID().equals(data)) {
                        String coordinates = pos.getCoordinates();
                        String a[] = coordinates.split(",");
                        location = new Location("dummyProvider");
                        location.setLatitude(Double.parseDouble(a[0]));
                        location.setLongitude(Double.parseDouble(a[1]));
                        Glide.with(PostFragment.this)
                                .load(pos.getPostImage())
                                .centerCrop()
                                .into(postImage);

                        description.setText(pos.getDesc());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null && !comment.getText().toString().equals("")) {
                    String uid = mAuth.getCurrentUser().getUid();
                    String userName = mAuth.getCurrentUser().getDisplayName();
                    String text = comment.getText().toString();
                    Comment newComment = new Comment(uid, userName, text);
                    comments.child(data).push().setValue(newComment);
                    comment.setText("");
                } else if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.must_be_logged), Snackbar.LENGTH_SHORT).show();
                else if (FirebaseAuth.getInstance().getCurrentUser() != null && comment.getText().toString().equals(""))
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.no_empty_comment), Snackbar.LENGTH_SHORT).show();
            }
        });

        comments.child(data).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Comment comment1 = snap.getValue(Comment.class);
                    commentList.add(comment1);
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
        ImageButton googlemaps = rootView.findViewById(R.id.googlemaps);
        googlemaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cordinatesToMaps();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        ImageButton waze = rootView.findViewById(R.id.waze);
        waze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                location.setLatitude(32.03140520);
//                location.setLongitude(34.74392110);
                cordinatesToWaze();
            }
        });

        ImageButton whastsapp = rootView.findViewById(R.id.whatsapp);
        whastsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView = rootView.findViewById(R.id.BigPostImage);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("image/*");
                Bitmap bitmap = loadBitmapFromView(imageView, imageView.getWidth(), imageView.getHeight()); // CREATE BITMAP
                sendIntent.putExtra(Intent.EXTRA_STREAM, SaveImage(bitmap));
                sendIntent.putExtra(Intent.EXTRA_TEXT, "#Thank4Giving\nIt's Free\nProduct:" + description.getText().toString() + "\nFind this Item in our Thank4Giving App and contact the Item owner.\nLets install our app from play store or click this link www.one.co.il");
                startActivity(sendIntent);
            }
        });
//        ImageButton facebook = rootView.findViewById(R.id.facebook);
//        facebook.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("image/*");
//                imageView = rootView.findViewById(R.id.BigPostImage);
//                imageView.setImageResource(R.drawable.tv);
//                Bitmap bitmap = loadBitmapFromView(imageView, imageView.getWidth(), imageView.getHeight()); // CREATE BITMAP
//                intent.putExtra(Intent.EXTRA_STREAM, SaveImage(bitmap));
//                intent.putExtra(Intent.EXTRA_TEXT, "http://www.one.co.il");
//                boolean facebookAppFound = false;
//                List<ResolveInfo> matches = getActivity().getPackageManager().queryIntentActivities(intent, 0);
//                for (ResolveInfo info : matches) {
//                    if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
//                        intent.setPackage(info.activityInfo.packageName);
//                        facebookAppFound = true;
//                        break;
//                    }
//                }
//                startActivity(intent);
//            }
//        });
        return rootView;
    }

    private void cordinatesToMaps() throws IOException {
        posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post pos = ds.getValue(Post.class);
                    if (pos.getPostID().equals(data)) {
                        if (pos.getLocationMethod().equals("GPS")) {
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
                            String url = "https://www.google.com/maps/search/?api=1&query=" + pos.getAddress();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

//                            String a[]=coordinates.split(",");
//                            location = new Location("dummyProvider");
//                            location.setLatitude(Double.parseDouble(a[0]));
//                            location.setLongitude(Double.parseDouble(a[1]));
                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public Uri SaveImage(Bitmap finalBitmap) {
        Random r = new Random();
        int low = 10;
        int high = 1000000;
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
        Uri imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
        Toast.makeText(getContext(), imageUri.toString(), Toast.LENGTH_SHORT).show();
        return imageUri;
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    private void cordinatesToWaze() {
        posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post pos = ds.getValue(Post.class);
                    if (pos.getPostID().equals(data)) {
//
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String url = "https://www.waze.com/ul?ll=" + latitude + "%2C" + longitude + "&navigate=yes";
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext());
        //postID = MainActivity.getPostClickedID();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private Bitmap addWaterMark(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Bitmap waterMark = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_share_24);
        //  canvas.drawBitmap(waterMark, 0, 0, null);
        int startX = (canvas.getWidth() - waterMark.getWidth()) / 2;//for horisontal position
        int startY = (canvas.getHeight() - waterMark.getHeight()) / 2;//for vertical position
        canvas.drawBitmap(waterMark, startX, startY, null);

        return result;
    }
}