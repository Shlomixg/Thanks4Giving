package com.tsk.thanks4giving;

import java.util.ArrayList;
import java.util.List;

public class Post {

    //for firebase usage
    private int ID;
    // the item image
    private String postImage;
    // the profile pic of the person who posted it
    private String profileImage;
    // number of likes
    private int likes;
    // list of Comment objects
    private ArrayList<Comment> comments;
    // list ID's watching this post
    private List<Integer> watching;

    public Post(String postImage, String profileImage, int likes, ArrayList<Comment> comments, List<Integer> watching) {
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

    public List<Integer> getWatching() {
        return watching;
    }

    public int getID() {
        return ID;
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

    public void setWatching(List<Integer> watching) {
        this.watching = watching;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void addToWatching(int id){
        watching.add(id);
    }
}
