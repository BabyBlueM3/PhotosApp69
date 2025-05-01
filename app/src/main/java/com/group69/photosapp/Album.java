package com.group69.photosapp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private String name;
    private List<PhotoFile> photos;
    private boolean isSelected;
    // Constructor
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
        this.isSelected = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PhotoFile> getPhotos() {
        return photos;
    }

    // Add a photo to the album
    public void addPhoto(PhotoFile photo) {
        this.photos.add(photo);
    }

    // Remove a photo from the album
    public void removePhoto(PhotoFile photo) {
        this.photos.remove(photo);
    }

    // These Selected methods are used in the home activity.
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    // Get the earliest date of photos in the album (converted to LocalDateTime)
    public LocalDateTime getEarliestDate() {
        if (photos.isEmpty()) return null;
        return photos.stream().map(PhotoFile::getDateTaken)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    // Get the latest date of photos in the album (converted to LocalDateTime)
    public LocalDateTime getLatestDate() {
        if (photos.isEmpty()) return null;
        return photos.stream().map(PhotoFile::getDateTaken)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Album [name=" + name + ", numberOfPhotos=" + photos.size() + "]";
    }
}
