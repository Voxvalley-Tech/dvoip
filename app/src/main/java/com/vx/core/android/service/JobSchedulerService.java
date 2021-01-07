package com.vx.core.android.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

import com.vx.core.android.utils.MethodHelper;


/**
 * This class creates job scheduler to check whether XMPP connection is active or not.
 *
 * @author Ramesh Reddy
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.e("JobScheduler", "onStartJob");

        try {
            // If background service is not running below code will restart again.
            if (!MethodHelper.isGivenServiceRunning(getApplicationContext(),
                    SIPService.class)) {
                MethodHelper.startSIPService(getApplicationContext());
            }

            // Rescheduling job scheduler
            //MethodHelper.createJobScheduler(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("JobScheduler", "onStopJob");

        return true;
    }

}
