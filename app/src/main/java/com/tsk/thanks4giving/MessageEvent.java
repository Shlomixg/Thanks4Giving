package com.tsk.thanks4giving;

import android.util.Log;

public class MessageEvent {

    public String msg;

    public MessageEvent(String message) {
        Log.d("ddd", "Message event constructor");
        this.msg = message;
    }

}
