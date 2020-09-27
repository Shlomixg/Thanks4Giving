package com.tsk.thanks4giving;

import android.net.Uri;

public class MessageEvent {

    String TAG1 = "signup";
    String TAG2 = "login";

    public String name;
    public Uri photoUrl;
    public String userToken;
    public String action;

    public MessageEvent(String username, String userToken) {
        name = username;
        this.userToken = userToken;
        action = TAG1;
    }

    public MessageEvent(String username, Uri photo, String userToken) {
        name = username;
        photoUrl = photo;
        this.userToken = userToken;
        action = TAG2;
    }

}
