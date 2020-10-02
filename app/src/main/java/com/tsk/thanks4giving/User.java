package com.tsk.thanks4giving;

import android.net.Uri;

public class User {

    public String name;
    public String token;
    public String email;
    public String gender;
    public String address;
    public Uri profilePhoto;

    public User() {

    }

    public User(String name, String token, String email, String gender, String Address, Uri profilePhoto) {
        this.name = name;
        this.token = token;
        this.email = email;
        this.gender = gender;
        address = Address;
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

    public String getToken() {
        return token;
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

    public void setToken(String token) {
        this.token = token;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAddress(String Address) {
        address = Address;
    }
}
