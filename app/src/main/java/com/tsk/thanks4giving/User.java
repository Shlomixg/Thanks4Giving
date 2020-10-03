package com.tsk.thanks4giving;

import android.net.Uri;

public class User {

    public String uid;
    public String name;
    public String email;
    public String gender;
    public String address;
    public Uri profilePhoto;

    public User() {

    }

    public User(String uid, String name, String email, String gender, String address, Uri profilePhoto) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.profilePhoto = profilePhoto;
    }

    /* Getters */

    public String getName() {
        return name;
    }

    public Uri getProfilePhoto() {
        return profilePhoto;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    /* Setters */

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePhoto(Uri profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAddress(String Address) {
        address = Address;
    }
}
