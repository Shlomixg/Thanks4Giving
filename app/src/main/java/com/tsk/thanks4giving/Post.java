package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Post {

    public String userUid; // UID of the user that posted
    public String title;
    public String desc;
    public int status;
    public String category;
    public String postImage; // the item image
    public ArrayList<String> likes; // list of the users which liked the post
    public ArrayList<Comment> comments; // list of Comment objects
    public ArrayList<String> watching;// list ID's watching this post

    public Post() {

    }

    public Post(String userUid, String title, String desc, int status, String category, String postImage) {
        this.userUid = userUid;
        this.status = status;
        this.title = title;
        this.desc = desc;
        this.category = category;
        this.postImage = postImage;
        this.likes = new ArrayList<String>();
        this.comments = new ArrayList<Comment>();
        this.watching = new ArrayList<String>();
    }

    /* Getters */

    public String getUserUid() {
        return userUid;
    }

    public int getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public String getPostImage() {
        return postImage;
    }

    public ArrayList<String> getWatching() {
        return watching;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    /* Setters */

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public void setWatching(ArrayList<String> watching) {
        this.watching = watching;
    }
}
