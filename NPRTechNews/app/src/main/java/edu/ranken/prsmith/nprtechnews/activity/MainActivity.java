package edu.ranken.prsmith.nprtechnews.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import edu.ranken.prsmith.nprtechnews.NprApp;
import edu.ranken.prsmith.nprtechnews.R;
import edu.ranken.prsmith.nprtechnews.adapter.StoryListAdapter;
import edu.ranken.prsmith.nprtechnews.model.Feed;
import edu.ranken.prsmith.nprtechnews.model.NprDataSource;
import edu.ranken.prsmith.nprtechnews.worker.GetFeedWorker;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "NPRTechNews";

    // views
    private RecyclerView recyclerView;

    // data
    private NprApp app;
    private NprDataSource dataSource;
    private LiveData<Feed> feedLiveData;
    private StoryListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find views
        recyclerView = findViewById(R.id.feed_recycler_view);

        // get data
        app = (NprApp) getApplication();
        dataSource = app.getDataSource();
        feedLiveData = dataSource.getFeedLiveData();

        // init recycler view
        adapter = new StoryListAdapter(this, null);
        recyclerView.setLayoutManager(new GridLayoutManager(
            this,
            this.getResources().getInteger(R.integer.item_story_grid_cols)
        ));
        recyclerView.setAdapter(adapter);

        // init adapter
        Feed feed = feedLiveData.getValue();
        if (feed != null) {
            adapter.setItems(feed.items);
        }

        // observe live data
        feedLiveData.observe(this, (Feed value) -> {
            adapter.setItems(value.items);
            Snackbar.make(recyclerView, R.string.feed_refreshed, Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                if (!isConnected()) {
                    Snackbar.make(recyclerView, R.string.error_no_network, Snackbar.LENGTH_SHORT).show();
                } else {
                    WorkManager workManager = WorkManager.getInstance(this);

                    OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(GetFeedWorker.class)
                            .build();

                    workManager.enqueue(workRequest);
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mgr.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        return isConnected;
    }
}
