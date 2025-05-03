package com.group69.photosapp;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to manage tags across the application
 * Handles adding, removing, and tracking tag usage
 */
public class TagManager {
    private static final String TAG = "TagManager";

    // Map to keep track of how many times each tag value is used
    private static Map<String, Integer> personTagUsage = new HashMap<>();
    private static Map<String, Integer> locationTagUsage = new HashMap<>();

    /**
     * Add a tag to the system and update usage counts
     * @param context Application context
     * @param tagName Tag type ("person" or "location")
     * @param tagValue Tag value to add
     */
    public static void addTag(Context context, String tagName, String tagValue) {
        if (tagValue == null || tagValue.isEmpty()) {
            return;
        }

        Database db = Database.getInstance();

        // Normalize tag value to lowercase
        tagValue = tagValue.trim().toLowerCase();

        if ("person".equals(tagName)) {
            // Add to database if not exists
            if (!db.getPersonTags().contains(tagValue)) {
                db.addPersonTag(tagValue);
            }

            // Update usage count
            Integer count = personTagUsage.getOrDefault(tagValue, 0);
            personTagUsage.put(tagValue, count + 1);

            Log.d(TAG, "Added person tag: " + tagValue + " (count: " + (count + 1) + ")");
        }
        else if ("location".equals(tagName)) {
            // Add to database if not exists
            if (!db.getLocationTags().contains(tagValue)) {
                db.addLocationTag(tagValue);
            }

            // Update usage count
            Integer count = locationTagUsage.getOrDefault(tagValue, 0);
            locationTagUsage.put(tagValue, count + 1);

            Log.d(TAG, "Added location tag: " + tagValue + " (count: " + (count + 1) + ")");
        }

        // Save changes to database
        db.save(context);
    }

    /**
     * Remove a tag from the system and update usage counts
     * Only removes from autocomplete list if this was the last occurrence
     * @param context Application context
     * @param tagName Tag type ("person" or "location")
     * @param tagValue Tag value to remove
     */
    public static void removeTag(Context context, String tagName, String tagValue) {
        if (tagValue == null || tagValue.isEmpty()) {
            return;
        }

        Database db = Database.getInstance();

        // Normalize tag value to lowercase
        tagValue = tagValue.trim().toLowerCase();

        if ("person".equals(tagName)) {
            // Update usage count
            Integer count = personTagUsage.getOrDefault(tagValue, 1); // Default to 1 to ensure removal
            count = Math.max(0, count - 1); // Prevent negative counts

            if (count == 0) {
                // Remove from database if this was the last occurrence
                db.removePersonTag(tagValue);
                personTagUsage.remove(tagValue);
                Log.d(TAG, "Removed person tag from autocomplete: " + tagValue);
            } else {
                personTagUsage.put(tagValue, count);
                Log.d(TAG, "Decreased person tag count: " + tagValue + " (count: " + count + ")");
            }
        }
        else if ("location".equals(tagName)) {
            // Update usage count
            Integer count = locationTagUsage.getOrDefault(tagValue, 1); // Default to 1 to ensure removal
            count = Math.max(0, count - 1); // Prevent negative counts

            if (count == 0) {
                // Remove from database if this was the last occurrence
                db.removeLocationTag(tagValue);
                locationTagUsage.remove(tagValue);
                Log.d(TAG, "Removed location tag from autocomplete: " + tagValue);
            } else {
                locationTagUsage.put(tagValue, count);
                Log.d(TAG, "Decreased location tag count: " + tagValue + " (count: " + count + ")");
            }
        }

        // Save changes to database
        db.save(context);
    }

    /**
     * Initialize tag usage counts from all photos in all albums
     * Should be called once at app startup
     */
    public static void initializeTagCounts() {
        personTagUsage.clear();
        locationTagUsage.clear();

        List<Album> albums = PhotoData.getInstance().getAlbums();

        for (Album album : albums) {
            for (PhotoFile photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    String tagName = tag.getTagName();
                    String tagValue = tag.getTagValue();

                    if ("person".equals(tagName)) {
                        Integer count = personTagUsage.getOrDefault(tagValue, 0);
                        personTagUsage.put(tagValue, count + 1);
                    }
                    else if ("location".equals(tagName)) {
                        Integer count = locationTagUsage.getOrDefault(tagValue, 0);
                        locationTagUsage.put(tagValue, count + 1);
                    }
                }
            }
        }

        Log.d(TAG, "Tag counts initialized: " + personTagUsage.size() + " person tags, " +
                locationTagUsage.size() + " location tags");
    }

    /**
     * Update usage counts when a photo is deleted
     * @param context Application context
     * @param photo Photo being deleted
     */
    public static void handlePhotoDeleted(Context context, PhotoFile photo) {
        // Process all tags in the photo
        for (Tag tag : photo.getTags()) {
            removeTag(context, tag.getTagName(), tag.getTagValue());
        }
    }
}
