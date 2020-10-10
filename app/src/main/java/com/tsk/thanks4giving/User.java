package com.tsk.thanks4giving;

import java.util.ArrayList;

public class User {

    public String uid;
    public String name;
    public String email;
    public String gender;
    public String address;
    public String coordinates;
    public String profilePhoto;
    public ArrayList<String> postsUid;

    public User() {

    }

    public User(String uid, String name, String email, String gender, String address,String coordinates ,String profilePhoto) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.coordinates=coordinates;
        this.profilePhoto = profilePhoto;
        this.postsUid = new ArrayList<String>();
    }

    /* Getters */

    public String getName() {
        return name;
    }

    public String getProfilePhoto() {
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

    public ArrayList<String> getPostsUid() {
        return postsUid;
    }

    /* Setters */

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePhoto(String profilePhoto) {
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

    public void setPostsUid(ArrayList<String> postsUid) {
        this.postsUid = postsUid;
    }
    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
}
