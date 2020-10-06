package com.tsk.thanks4giving;

import android.location.Location;
import android.util.Log;

public class MessageEvent {

    public Location location;

    public MessageEvent(Location loc) {
        Log.d("ddd", "Message event constructor");
        this.location = loc;
    }
}
