package com.group69.photosapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Database implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILENAME = "photo_database.dat";

    private static Database instance;

    private ArrayList<Album> albums;
    private HashSet<String> personTags;
    private HashSet<String> locationTags;

    private Database() {
        albums = new ArrayList<>();
        personTags = new HashSet<>();
        locationTags = new HashSet<>();
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // Save the entire database to internal storage
    public void save(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(DATA_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(instance);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load the database from internal storage
    public static void load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(DATA_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            instance = (Database) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            instance = new Database(); // fallback if no saved data yet
        }
    }

    // --- Album operations ---
    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
    }

    // --- Tag operations ---
    public HashSet<String> getPersonTags() {
        return personTags;
    }

    public HashSet<String> getLocationTags() {
        return locationTags;
    }

    public void addPersonTag(String tag) {
        if (tag != null && !tag.isEmpty()) {
            personTags.add(tag);
        }
    }

    public void addLocationTag(String tag) {
        if (tag != null && !tag.isEmpty()) {
            locationTags.add(tag);
        }
    }

    public void removePersonTag(String tag) {
        personTags.remove(tag);
    }

    public void removeLocationTag(String tag) {
        locationTags.remove(tag);
    }


}
