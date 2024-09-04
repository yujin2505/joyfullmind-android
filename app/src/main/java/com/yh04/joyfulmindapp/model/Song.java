package com.yh04.joyfulmindapp.model;

public class Song {
    private String name;
    private String artists;
    private String preview_url;
    private String album_cover_url;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public String getAlbum_cover_url() {
        return album_cover_url;
    }

    public void setAlbum_cover_url(String album_cover_url) {
        this.album_cover_url = album_cover_url;
    }
}

