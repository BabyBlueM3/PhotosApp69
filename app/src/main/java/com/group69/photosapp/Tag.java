package com.group69.photosapp;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable {
    private String tagName;
    private String tagValue;

    // Constructor that initializes tagName and tagValue
    public Tag(String tagName, String tagValue) {
        if (tagName == null || tagValue == null) {
            throw new IllegalArgumentException("Tag name and value cannot be null");
        }
        this.tagName = tagName.toLowerCase();  // Normalize for consistency
        this.tagValue = tagValue.toLowerCase();
    }

    // Getters (no setters as the class is immutable)
    public String getTagName() {
        return tagName;
    }

    public String getTagValue() {
        return tagValue;
    }

    @Override
    public String toString() {
        return tagName + "=" + tagValue;
    }

    // Prevent duplicates based on tag name and value
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        return tagName.equals(other.tagName) && tagValue.equals(other.tagValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, tagValue);
    }
}
