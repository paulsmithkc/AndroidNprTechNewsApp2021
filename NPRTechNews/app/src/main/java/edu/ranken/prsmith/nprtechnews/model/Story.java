package edu.ranken.prsmith.nprtechnews.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Story implements Serializable {
    public String id;
    public String url;
    public String title;
    public String summary;
    public String date_published; // optional
    public Author author; // optional
    public ArrayList<String> tags; // optional
}
