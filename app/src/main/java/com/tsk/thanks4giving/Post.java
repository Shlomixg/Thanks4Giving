package com.tsk.thanks4giving;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Post implements Parcelable {

    public String postID;
    public String userUid; // UID of the user that posted
    public String title;
    public String desc; // Description for the item
    public String address;
    public String coordinates;
    public String locationMethod;
    public String date;
    public int status; // 1 = available, 0 = not available
    public int category; // Items general category
    public String postImage; // Item image

    public Post() {

    }

    public Post(String postID, String userUid, String title, String desc, String address, String coordinates, String locationMethod, String date, int status, int category, String postImage) {
        this.postID = postID;
        this.userUid = userUid;
        this.status = status;
        this.title = title;
        this.desc = desc;
        this.address = address;
        this.coordinates = coordinates;
        this.locationMethod = locationMethod;
        this.date = date;
        this.category = category;
        this.postImage = postImage;
    }

    /* Getters */

    public String getPostID() {
        return postID;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getAddress() {
        return address;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getLocationMethod() {
        return locationMethod;
    }

    public String getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public int getCategory() {
        return category;
    }

    public String getPostImage() {
        return postImage;
    }

    /* Setters */

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setLocationMethod(String locationMethod) {
        this.locationMethod = locationMethod;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}