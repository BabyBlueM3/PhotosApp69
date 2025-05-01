package com.group69.photosapp;


import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PhotoFile implements Serializable {
    private String filePath;
    private String caption;
    private LocalDateTime dateTaken;
    private List<Tag> tags;

    // Constructor to initialize photo object
    public PhotoFile(String filePath, String caption) {
        this.filePath = filePath;
        this.caption = caption;
        this.dateTaken = getFileModificationDate(filePath);
        this.tags = new ArrayList<>();
    }

    // Method to get the file's last modification time
    private LocalDateTime getFileModificationDate(String filePath) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(Paths.get(filePath));
            LocalDateTime date = fileTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            return date.withNano(0); // Normalize the time to avoid milliseconds
        } catch (IOException e) {
            e.printStackTrace();
            return LocalDateTime.now();  // Default to current time if error occurs
        }
    }

    // Getters and Setters
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.dateTaken = getFileModificationDate(filePath);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(LocalDateTime dateTaken) {
        this.dateTaken = dateTaken;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    @Override
    public String toString() {
        return "PhotoFile [filePath=" + filePath + ", caption=" + caption + ", dateTaken=" + dateTaken + "]";
    }
}
