package edu.ranken.prsmith.nprtechnews.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.ranken.prsmith.nprtechnews.R;
import edu.ranken.prsmith.nprtechnews.model.Author;
import edu.ranken.prsmith.nprtechnews.model.Story;

public class StoryListAdapter extends RecyclerView.Adapter<StoryViewHolder> {
    private static final String LOG_TAG = "NPRTechNews";
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = 1000 * 60;
    private static final long MILLIS_PER_HOUR   = 1000 * 60 * 60;
    private static final long MILLIS_PER_DAY    = 1000 * 60 * 60 * 24;

    private ArrayList<Story> items;
    private Context context;
    private LayoutInflater layoutInflater;
    private Picasso picasso;

    public StoryListAdapter(Context context, ArrayList<Story> items) {
        this.items = items;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.picasso = Picasso.get();
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
        vh.image = itemView.findViewById(R.id.item_story_image);

        itemView.setOnClickListener((View v) -> {
            Story item = items.get(vh.getAdapterPosition());
            Uri uri = Uri.parse(item.url);
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder vh, int position) {
        Story item = items.get(position);
        vh.title.setText(item.title);
        vh.summary.setText(item.summary);

        String published = item.date_published;
        if (published != null) {
            vh.date_published.setText(formatPublished(published));
            vh.date_published.setVisibility(View.VISIBLE);
        } else {
            vh.date_published.setText("");
            vh.date_published.setVisibility(View.GONE);
        }

        Author author = item.author;
        if (author != null) {
            vh.author.setText(author.name);
            vh.author.setVisibility(View.VISIBLE);
        } else {
            vh.author.setText(context.getText(R.string.author_npr));
            vh.author.setVisibility(View.VISIBLE);
        }

        ArrayList<String> tags = item.tags;
        if (tags != null) {
            vh.tags.setText(joinTags(tags));
            vh.tags.setVisibility(View.VISIBLE);
        } else {
            vh.tags.setText("");
            vh.tags.setVisibility(View.GONE);
        }

        String imageUrl = item.image;
        if (imageUrl != null) {
            vh.image.setVisibility(View.VISIBLE);
            picasso
                .load(imageUrl)
                .placeholder(R.drawable.ic_downloading)
                .error(R.drawable.ic_error)
                //.resize(1000, 1000)
                .resizeDimen(R.dimen.item_story_image_resize, R.dimen.item_story_image_resize)
                .centerInside()
                .into(vh.image);
        } else {
            vh.image.setVisibility(View.GONE);
            vh.image.setImageResource(R.drawable.ic_error);
        }
    }

    private CharSequence joinTags(ArrayList<String> tags) {
        StringBuilder sb = new StringBuilder();
        if (tags != null) {
            for (String tag : tags) {
                sb.append(tag).append(", ");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2);
            }
        }
        return sb;
    }

    private CharSequence formatPublished(String published) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
            Date date = format.parse(published);
            long elapsedMillis = System.currentTimeMillis() - date.getTime();
            if (elapsedMillis >= MILLIS_PER_DAY) {
                return context.getString(R.string.published_time_days, elapsedMillis / MILLIS_PER_DAY);
            } else if (elapsedMillis >= MILLIS_PER_HOUR) {
                return context.getString(R.string.published_time_hours, elapsedMillis / MILLIS_PER_HOUR);
            } else if (elapsedMillis >= MILLIS_PER_MINUTE) {
                return context.getString(R.string.published_time_minutes, elapsedMillis / MILLIS_PER_MINUTE);
            } else {
                return context.getString(R.string.published_time_seconds, elapsedMillis / MILLIS_PER_SECOND);
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "failed to parse/format timestamp", ex);
            return published;
        }
    }
}
