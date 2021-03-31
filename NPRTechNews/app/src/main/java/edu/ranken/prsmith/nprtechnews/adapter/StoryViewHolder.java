package edu.ranken.prsmith.nprtechnews.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StoryViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView summary;
    public TextView author;
    public TextView date_published;
    public TextView tags;
    public ImageView image;

    public StoryViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
