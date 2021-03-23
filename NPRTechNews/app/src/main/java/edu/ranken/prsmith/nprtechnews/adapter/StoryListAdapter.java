package edu.ranken.prsmith.nprtechnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.ranken.prsmith.nprtechnews.R;
import edu.ranken.prsmith.nprtechnews.model.Story;

public class StoryListAdapter extends RecyclerView.Adapter<StoryViewHolder> {

    private ArrayList<Story> items;
    private Context context;
    private LayoutInflater layoutInflater;

    public StoryListAdapter(Context context, ArrayList<Story> items) {
        this.items = items;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setItems(ArrayList<Story> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_story, parent, false);

        StoryViewHolder vh = new StoryViewHolder(itemView);
        vh.title = itemView.findViewById(R.id.item_story_title);
        vh.summary = itemView.findViewById(R.id.item_story_summary);
        vh.author = itemView.findViewById(R.id.item_story_author);
        vh.date_published = itemView.findViewById(R.id.item_story_published);
        vh.tags = itemView.findViewById(R.id.item_story_tags);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder vh, int position) {
        Story item = items.get(position);
        vh.title.setText(item.title);
        vh.summary.setText(item.summary);
        vh.author.setText(item.author.name);
        vh.date_published.setText(item.date_published);
        vh.tags.setText(joinTags(item.tags));
    }

    private CharSequence joinTags(ArrayList<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append(tag).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb;
    }
}
