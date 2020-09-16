package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Post {

    //the item image
    private String postImage;
    //the profile pic of the person who posted it
    private String profileImage;
    //number of likes
    private int likes;
    //list of Comment objects
    private ArrayList<Comment> comments;
    //list of people watching this post
    private ArrayList<Profile> watching;

    public Post(String postImage, String profileImage, int likes, ArrayList<Comment> comments, ArrayList<Profile> watching) {
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.likes = likes;
        this.comments = comments;
        this.watching = watching;
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

    public ArrayList<Profile> getWatching() {
        return watching;
    }

    public void setWatching(ArrayList<Profile> watching) {
        this.watching = watching;
    }
}
