package com.yh04.joyfulmindapp.model;

import java.util.List;

public class DiaryResponse {
    private String result;
    private List<Diary> items;
    private int count;

    // Getters and setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Diary> getItems() {
        return items;
    }

    public void setItems(List<Diary> items) {
        this.items = items;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
