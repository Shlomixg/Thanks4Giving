package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Post {

    public String postID;
    public String userUid; // UID of the user that posted
    public String title; //TODO  ???
    public String desc; //description for the item
    public int status; // 1 = available 0 = not available
    public String category; //items general category
    public String postImage; // Item image
    public ArrayList<String> likes; // List of users which liked the post
    public ArrayList<Comment> comments; // List of Comment objects
    public ArrayList<String> watching;// List ID's watching this post

    public Post() {

    }

    public Post(String postid, String userUid, String title, String desc, int status, String category, String postImage) {
        this.postID = postid;
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

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getPostID() {
        return postID;
    }

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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

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
