package com.group69.photosapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String username;
    private List<Album> albums;

    // Constructor
    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    // Add an album to the user
    public void addAlbum(Album album) {
        this.albums.add(album);
    }

    // Remove an album from the user
    public void removeAlbum(Album album) {
        this.albums.remove(album);
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", numberOfAlbums=" + albums.size() + "]";
    }
}
