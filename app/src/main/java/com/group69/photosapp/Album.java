package com.group69.photosapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private String name;
    private List<PhotoFile> photos;
    private boolean isSelected;
    private boolean isTemporary;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
        this.isSelected = false;
        this.isTemporary = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PhotoFile> getPhotos() {
        return photos;
    }

    public void addPhoto(PhotoFile photo) {
        photos.add(photo);
    }

    public void removePhoto(PhotoFile photo) {
        photos.remove(photo);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    public boolean containsPhoto(PhotoFile photo) {
        for (PhotoFile p : photos) {
            if (p.getFilePath().equals(photo.getFilePath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + name + '\'' +
                ", photosCount=" + photos.size() +
                ", isTemporary=" + isTemporary +
                '}';
    }
}