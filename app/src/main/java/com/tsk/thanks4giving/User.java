package com.tsk.thanks4giving;

import android.net.Uri;

public class User {

    public String name;
    public String userUid;
    public Uri profilePhoto;

    public User(String name, String userUid) {
        this.name = name;
        this.userUid = userUid;
        this.profilePhoto = profilePhoto;
    }

    /* Getters */

    public String getName() {
        return name;
    }

    public String getUserUid() {
        return userUid;
    }

    public Uri getProfilePhoto() {
        return profilePhoto;
    }

    /* Setters */

    public void setName(String name) {
        this.name = name;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public void setProfilePhoto(Uri profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
