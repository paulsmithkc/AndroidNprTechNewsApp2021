package edu.ranken.prsmith.nprtechnews.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import edu.ranken.prsmith.nprtechnews.NprApp;
import edu.ranken.prsmith.nprtechnews.R;
import edu.ranken.prsmith.nprtechnews.adapter.StoryListAdapter;
import edu.ranken.prsmith.nprtechnews.model.Feed;
import edu.ranken.prsmith.nprtechnews.model.NprDataSource;

public class MainActivity extends AppCompatActivity {

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // init adapter
        Feed feed = feedLiveData.getValue();
        if (feed != null) {
            adapter.setItems(feed.items);
        }

        // observe live data
        feedLiveData.observe(this, (Feed value) -> {
            adapter.setItems(value.items);
        });
    }
}
