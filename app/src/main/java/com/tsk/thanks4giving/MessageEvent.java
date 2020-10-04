package com.tsk.thanks4giving;

import android.util.Log;

public class MessageEvent {

    public Post post;

    public MessageEvent(Post post) {
        Log.d("post","Message event constructor " + post.getPostID());
        this.post = post;
    }
}
