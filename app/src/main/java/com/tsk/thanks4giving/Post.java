package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Post {

    // the item image
    private String postImage;
    // the profile pic of the person who posted it
    private String profileImage;
    // number of likes
    private int likes;
    // list of Comment objects
    private ArrayList<Comment> comments;
    // list of people watching this post
    private ArrayList<Profile> watching;

    public Post(String postImage, String profileImage, int likes, ArrayList<Comment> comments, ArrayList<Profile> watching) {
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.likes = likes;
        this.comments = comments;
        this.watching = watching;
    }

    /* Getters */

    public String getPostImage() {
        return postImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public int getLikes() {
        return likes;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public ArrayList<Profile> getWatching() {
        return watching;
    }

    /* Setters */

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public void setWatching(ArrayList<Profile> watching) {
        this.watching = watching;
    }
}
