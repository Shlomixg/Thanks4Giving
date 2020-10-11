package com.tsk.thanks4giving;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;

public class GoogleConnectionRefreshService extends JobService {

    final String MESSAGE  = "refresh connection with google servers";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("ddd", "job started");
        EventBus.getDefault().post(new MessageEvent(MESSAGE));
        Log.d("ddd", "message sent");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("ddd", "job on stop");
        return true;
    }

}