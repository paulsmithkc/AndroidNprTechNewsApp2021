package edu.ranken.prsmith.nprtechnews;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.TimeUnit;

import edu.ranken.prsmith.nprtechnews.model.NprDataSource;
import edu.ranken.prsmith.nprtechnews.worker.GetFeedWithVolleyWorker;
import edu.ranken.prsmith.nprtechnews.worker.GetFeedWorker;

public class NprApp extends Application {
    public static final String LOG_TAG = "NPRTechNews";
    public static final String JOB_NAME_WATCH_FEED = "watchFeed";
    public static final String NEW_STORY_NOTIFICATION_CHANNEL_ID = "newStory";
    public static final int NEW_STORY_NOTIFICATION_ID = 2;

    private NprDataSource dataSource;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        dataSource = new NprDataSource();
        requestQueue = Volley.newRequestQueue(this);

        createNotificationChannels();
        enqueueWorkers();
    }

    public NprDataSource getDataSource() {
        return dataSource;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager mgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(
                NEW_STORY_NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_new_story_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.notification_new_story_channel_description));

            mgr.createNotificationChannel(channel);
        }
    }

    private void enqueueWorkers() {

        WorkManager workManager = WorkManager.getInstance(this);

        Constraints watchFortuneConstraints =
            new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest watchFortuneRequest =
            new PeriodicWorkRequest.Builder(GetFeedWithVolleyWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(watchFortuneConstraints)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .build();

        workManager.enqueueUniquePeriodicWork(
            JOB_NAME_WATCH_FEED,
            ExistingPeriodicWorkPolicy.REPLACE,
            watchFortuneRequest
        );

        Log.i(LOG_TAG, "worker enqueued");
    }
}
