package com.tsk.thanks4giving;

import java.util.ArrayList;

public class Post {

    public String postID;
    public String userUid; // UID of the user that posted
    public String desc; // Description for the item
    public String address;
    public String coordinates;
    public String locationMethod;
    public int status; // 1 = available 0 = not available
    public String category; // items general category
    public String postImage; // Item image
    public ArrayList<String> likes; // List of users which liked the post
    public ArrayList<Comment> comments; // List of Comment objects
    public ArrayList<String> watching;// List ID's watching this post

    public Post() {

    }

    public Post(String postid, String userUid, String desc, String address, String coordinates, String locationMethod, int status, String category, String postImage) {
        this.postID = postid;
        this.userUid = userUid;
        this.status = status;
        this.desc = desc;
        this.address = address;
        this.coordinates = coordinates;
        this.locationMethod = locationMethod;
        this.category = category;
        this.postImage = postImage;
        this.likes = new ArrayList<String>();
        this.comments = new ArrayList<Comment>();
        this.watching = new ArrayList<String>();
    }

    /* Getters */

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getLocationMethod() {
        return locationMethod;
    }

    public void setLocationMethod(String locationMethod) {
        this.locationMethod = locationMethod;
    }
}