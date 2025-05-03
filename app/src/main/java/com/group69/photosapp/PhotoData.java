package com.group69.photosapp;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PhotoData implements Serializable {
    private static final PhotoData instance = new PhotoData();
    private List<Album> albums;

    private PhotoData() {
        albums = new ArrayList<>();
    }

    public static PhotoData getInstance() {
        return instance;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void addAlbum(Album album) {
        if (!albums.contains(album)) {
            albums.add(album);
        }
    }

    public void removeAlbum(String albumName) {
        albums.removeIf(album -> album.getName().equals(albumName));
    }

    public void removeAlbum(Album albumToRemove) {
        albums.remove(albumToRemove);
    }

    public Album getAlbumByName(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }
        return null;
    }
    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }


    // Stubbed for now, implement file I/O if needed later
    public void saveData(Context context) {
        // Save the data to the Database
        Database.getInstance().setAlbums(new ArrayList<>(getAlbums()));
        Database.getInstance().save(context);
    }

    public void loadData() {
        // TODO: load albums from storage if implementing persistence
    }
}
