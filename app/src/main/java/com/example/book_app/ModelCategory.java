package com.example.book_app;

import android.view.Display;

public class ModelCategory {
    String id;
    String category;
    String timestamp;
    String uid;
    public ModelCategory(){}

    public ModelCategory(String id, String category, String timestamp, String uid) {
        this.id = id;
        this.category = category;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
