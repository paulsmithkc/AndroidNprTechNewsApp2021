package edu.ranken.prsmith.nprtechnews.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class Story implements Serializable {
    public String id;
    public String url;
    public String title;
    public String summary;
    @Nullable public String date_published; // optional
    @Nullable public Author author; // optional
    @Nullable public ArrayList<String> tags; // optional
    @Nullable public String image; // optional
}
