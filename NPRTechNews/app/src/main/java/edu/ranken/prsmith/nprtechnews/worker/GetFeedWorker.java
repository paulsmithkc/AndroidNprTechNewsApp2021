package edu.ranken.prsmith.nprtechnews.worker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import edu.ranken.prsmith.nprtechnews.NprApp;
import edu.ranken.prsmith.nprtechnews.activity.MainActivity;
import edu.ranken.prsmith.nprtechnews.R;
import edu.ranken.prsmith.nprtechnews.model.Feed;
import edu.ranken.prsmith.nprtechnews.model.NprDataSource;
import edu.ranken.prsmith.nprtechnews.model.Story;

public class GetFeedWorker extends Worker {
    private static final String LOG_TAG = "NPRTechNews";
    private static final String BASE_URL = "https://feeds.npr.org/1019/feed.json";

    private NprApp app;
    private NprDataSource dataSource;

    public GetFeedWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        app = (NprApp) context;
        dataSource = app.getDataSource();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(LOG_TAG, "checking for new fortune");
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL);

            // connect
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(60_000);
            connection.setReadTimeout(15_000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            // read and parse response
            try (InputStream stream = connection.getInputStream()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                    Gson gson = new Gson();
                    Feed newData = gson.fromJson(reader, Feed.class);

                    // is this new data?
                    Feed oldData = dataSource.getFeed();
                    if (oldData == null) {
                        // first time reading the feed
                        Log.i(LOG_TAG, "feed initialized");
                        dataSource.setFeed(newData);
                    } else if (Objects.equals(newData.items.get(0).id, oldData.items.get(0).id)) {
                        // nothing changed
                        Log.i(LOG_TAG, "nothing new");
                    } else {
                        // new fortune, update and send notification
                        Log.i(LOG_TAG, "new story!");
                        dataSource.setFeed(newData);
                        showNewStoryNotification(getApplicationContext(), newData);
                    }
                }
            }

            // success
            return Result.success();

        } catch (MalformedURLException ex) {
            Log.i(LOG_TAG, "error getting fortune, failed due to bad url", ex);
            return Result.failure();
        } catch (IOException ex) {
            Log.i(LOG_TAG, "error getting fortune, retrying due to IO error", ex);
            return Result.retry();
        } catch (Exception ex) {
            Log.i(LOG_TAG, "error getting fortune, failed for other reason", ex);
            return Result.failure();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void showNewStoryNotification(Context context, Feed feed) {
        // open MainActivity when the user taps on the notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // get the newest story
        Story story = feed.items.get(0);

        // build notification
        Notification notification =
            new NotificationCompat.Builder(context, NprApp.NEW_STORY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(story.title)
                .setContentText(story.summary)
                .setSmallIcon(R.drawable.npr)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        // show notification now
        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(NprApp.NEW_STORY_NOTIFICATION_ID, notification);
    }
}
