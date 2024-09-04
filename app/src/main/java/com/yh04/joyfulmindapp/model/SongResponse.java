package com.yh04.joyfulmindapp.model;

import java.util.List;

public class SongResponse {
    private String emotion;
    private List<Song> songs;

    // Getters and setters
    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
