package com.tsk.thanks4giving;

import java.util.ArrayList;
import java.util.List;

public class Post {

    //for firebase usage, idToken of the user that posted
    private String posterToken;
    // the item image
    private String postImage;
    // the profile pic of the person who posted it
    private String profileImage;
    // number of likes
    private int likes;
    // list of Comment objects
    private ArrayList<Comment> comments;
    // list ID's watching this post
    private List<String> watching;

    public Post(String posterToken, String postImage, String profileImage, int likes, ArrayList<Comment> comments, List<String> watching) {
        this.posterToken = posterToken;
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.likes = likes;
        this.comments = comments;
        this.watching = watching;
    }

    public String getPosterToken() {
        return posterToken;
    }

    public void setPosterToken(String posterToken) {
        this.posterToken = posterToken;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public List<String> getWatching() {
        return watching;
    }

    public void setWatching(List<String> watching) {
        this.watching = watching;
    }

}
