package edu.ranken.prsmith.nprtechnews.worker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;

import edu.ranken.prsmith.nprtechnews.NprApp;
import edu.ranken.prsmith.nprtechnews.R;
import edu.ranken.prsmith.nprtechnews.activity.MainActivity;
import edu.ranken.prsmith.nprtechnews.model.Feed;
import edu.ranken.prsmith.nprtechnews.model.NprDataSource;
import edu.ranken.prsmith.nprtechnews.model.Story;
import edu.ranken.prsmith.nprtechnews.request.GsonRequest;

public class GetFeedWithVolleyWorker extends ListenableWorker {
    private static final String LOG_TAG = "NPRTechNews";
    private static final String BASE_URL = "https://feeds.npr.org/1019/feed.json";

    private NprApp app;
    private NprDataSource dataSource;
    private RequestQueue requestQueue;

    public GetFeedWithVolleyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        app = (NprApp) context;
        dataSource = app.getDataSource();
        requestQueue = app.getRequestQueue();
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(resolver -> {
            Request request = new GsonRequest<Feed>(
                Request.Method.GET,
                BASE_URL,
                Feed.class,
                null,
                null,
                (Feed feed) -> {
                    processFeed(feed);
                    resolver.set(Result.success());

                },
                (VolleyError error) -> {
                    Log.e(LOG_TAG, "error getting news feed", error);
                    resolver.set(Result.retry());
                }
            );

            requestQueue.add(request);
            return request;
        });
    }

    private void processFeed(Feed newData) {
        // is this new data?
        Feed oldData = dataSource.getFeed();
        if (oldData == null) {
            // first time reading the feed
            Log.i(LOG_TAG, "feed initialized");
            dataSource.setFeed(newData);
        } else if (Objects.equals(newData.items.get(0).id, oldData.items.get(0).id)) {
            // nothing changed
            Log.i(LOG_TAG, "nothing new");
            dataSource.setFeed(newData);
        } else {
            // new story, update and send notification
            Log.i(LOG_TAG, "new story!");
            dataSource.setFeed(newData);
            showNewStoryNotification(getApplicationContext(), newData);
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

        // configure style
        NotificationCompat.Style style =
            new NotificationCompat.BigTextStyle()
                .setBigContentTitle(story.title)
                .setSummaryText(story.summary)
                .bigText("\n" + story.summary);

        // build notification
        Notification notification =
            new NotificationCompat.Builder(context, NprApp.NEW_STORY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(story.title) // MUST BE SET FOR COMPATIBILITY
                .setContentText(story.summary) // MUST BE SET FOR COMPATIBILITY
                .setSmallIcon(R.drawable.npr)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(style)
                .build();

        // show notification now
        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(NprApp.NEW_STORY_NOTIFICATION_ID, notification);
    }
}
