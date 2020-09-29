package com.tsk.thanks4giving;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;

public class MessageUserEvent {

    String TAG1 = "signup";
    String TAG2 = "login";

    public User user;
    public Uri photo;
    public String action;

    public MessageUserEvent(User user) {
        this.user = user;
    }

}
