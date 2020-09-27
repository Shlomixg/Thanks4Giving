package com.tsk.thanks4giving;

import android.net.Uri;

public class User {

    public String name;
    public String tokenID;
    public Uri profilePhoto;

    public User(String name, String tokenID) {
        this.name = name;
        this.tokenID = tokenID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public Uri getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(Uri profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
