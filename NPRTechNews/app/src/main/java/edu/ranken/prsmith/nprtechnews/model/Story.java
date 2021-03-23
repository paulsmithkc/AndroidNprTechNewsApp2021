package edu.ranken.prsmith.nprtechnews.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Story implements Serializable {
    public String id;
    public String url;
    public String title;
    public String summary;
    public String date_published;
    public Author author;
    public ArrayList<String> tags;
}
