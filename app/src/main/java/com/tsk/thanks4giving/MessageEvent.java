package com.tsk.thanks4giving;

import android.net.Uri;


public class MessageEvent {

    String TAG1 = "signup";
    String TAG2 = "login";

    public String name;
    public Uri photoUrl;
    public String userToken;
    public String action;

    public MessageEvent(String username, String usertoken)
    {
        name = username;
        userToken = usertoken;
        action = TAG1;
    }

    public MessageEvent(String username, Uri photo, String usertoken)
    {
        name = username;
        photoUrl = photo;
        userToken = usertoken;
        action = TAG2;
    }

}
