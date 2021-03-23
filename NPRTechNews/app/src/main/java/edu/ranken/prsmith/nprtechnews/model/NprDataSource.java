package edu.ranken.prsmith.nprtechnews.model;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NprDataSource {
    private MutableLiveData<Feed> feedLiveData;

    public NprDataSource() {
        feedLiveData = new MutableLiveData<>();
    }

    public LiveData<Feed> getFeedLiveData() {
        return feedLiveData;
    }

    public Feed getFeed() {
        return feedLiveData.getValue();
    }

    @WorkerThread
    public void setFeed(Feed feed) {
        this.feedLiveData.postValue(feed);
    }
}
