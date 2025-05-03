package com.group69.photosapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.group69.photosapp.Album;
import com.group69.photosapp.Database;
import com.group69.photosapp.PhotoData;
import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;
import com.group69.photosapp.Tag;
import com.group69.photosapp.TagManager;

import java.util.ArrayList;
import java.util.List;

public class ViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView personTagValue;
    private TextView locationTagValue;
    private Button editPersonButton;
    private Button editLocationButton;
    private Button slideshowLeft;
    private Button slideshowRight;

    private List<PhotoFile> photoList;
    private int currentIndex;

    // Tag constants
    private static final String TAG_PERSON = "person";
    private static final String TAG_LOCATION = "location";
    private static final String TAG = "ViewActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // Load the database to get stored tags
        Database.load(getApplicationContext());

        // Register back press callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        // Initialize UI components
        initializeViews();

        // Get the PhotoFile from intent
        handleIntent();

        // Set up click listeners for edit buttons
        setupClickListeners();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        personTagValue = findViewById(R.id.personTagValue);
        locationTagValue = findViewById(R.id.locationTagValue);
        editPersonButton = findViewById(R.id.editPersonButton);
        editLocationButton = findViewById(R.id.editLocationButton);
        slideshowLeft = findViewById(R.id.slideshowLeft);
        slideshowRight = findViewById(R.id.slideshowRight);
    }

    @SuppressWarnings("unchecked")
    private void handleIntent() {
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("photoList") && intent.hasExtra("photoIndex")) {
            photoList = (ArrayList<PhotoFile>) intent.getSerializableExtra("photoList");
            currentIndex = intent.getIntExtra("photoIndex", 0);

            if (photoList == null || photoList.isEmpty()) {
                Toast.makeText(this, "No photos to display", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (currentIndex < 0 || currentIndex >= photoList.size()) {
                Toast.makeText(this, "Invalid photo index", Toast.LENGTH_SHORT).show();
                currentIndex = 0;
            }

            loadImage();
            updateTagsDisplay();
        } else {
            Toast.makeText(this, "Missing photo data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImage() {
        updateTagsDisplay();

        try {
            PhotoFile photoFile = photoList.get(currentIndex);

            // Load image from file path
            Uri imageUri = Uri.parse("file://" + photoFile.getFilePath());
            imageView.setImageURI(imageUri);

            // Set caption as title if available
            String caption = photoFile.getCaption();
            if (caption != null && !caption.isEmpty()) {
                setTitle(caption);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading image", e);
        }
    }

    private void updateTagsDisplay() {
        PhotoFile currentPhoto = photoList.get(currentIndex);
        String personValue = findTagValue(currentPhoto, TAG_PERSON);
        String locationValue = findTagValue(currentPhoto, TAG_LOCATION);

        // Update UI with tag values
        personTagValue.setText(personValue.isEmpty() ? "No person tagged" : personValue);
        locationTagValue.setText(locationValue.isEmpty() ? "No location tagged" : locationValue);
    }

    private String findTagValue(PhotoFile photo, String tagName) {
        List<Tag> tags = photo.getTags();
        for (Tag tag : tags) {
            if (tag.getTagName().equalsIgnoreCase(tagName)) {
                return tag.getTagValue();
            }
        }
        return "";
    }

    private void setupClickListeners() {
        editPersonButton.setOnClickListener(v -> showEditTagDialog(TAG_PERSON));
        editLocationButton.setOnClickListener(v -> showEditTagDialog(TAG_LOCATION));

        slideshowLeft.setOnClickListener(v -> showPreviousPhoto());
        slideshowRight.setOnClickListener(v -> showNextPhoto());
    }

    private void showNextPhoto() {
        if (currentIndex < photoList.size() - 1) {
            currentIndex++;
            loadImage();
            updateTagsDisplay();
        } else {
            Toast.makeText(this, "This is the last photo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPreviousPhoto() {
        if (currentIndex > 0) {
            currentIndex--;
            loadImage();
            updateTagsDisplay();
        } else {
            Toast.makeText(this, "This is the first photo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditTagDialog(String tagType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        boolean isPerson = tagType.equals(TAG_PERSON);
        PhotoFile currentPhoto = photoList.get(currentIndex);
        String currentValue = isPerson ? findTagValue(currentPhoto, TAG_PERSON) : findTagValue(currentPhoto, TAG_LOCATION);

        builder.setTitle(isPerson ? "Edit Person Tag" : "Edit Location Tag");

        // Set up the input
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_tag, null);
        EditText input = dialogView.findViewById(R.id.tagEditText);
        input.setHint(isPerson ? "Enter person name" : "Enter location");
        input.setText(currentValue);
        builder.setView(dialogView);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = input.getText().toString().trim();

            // Check if we're changing the tag
            boolean isChangingTag = !currentValue.equals(newValue);

            if (isChangingTag) {
                // If current value exists, remove it from tag count
                if (!currentValue.isEmpty()) {
                    removeTag(tagType, currentValue);
                }

                // Add the new tag if not empty
                if (!newValue.isEmpty()) {
                    // Update or add the tag
                    updateTag(tagType, newValue);
                } else {
                    // Remove tag if value is empty
                    removeTag(tagType);
                }

                updateTagsDisplay();
                savePhotoChanges(); // Save changes immediately after editing
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        if (!currentValue.isEmpty()) {
            // Add delete button if tag exists
            builder.setNeutralButton("Delete", (dialog, which) -> {
                removeTag(tagType);
                updateTagsDisplay();
                savePhotoChanges(); // Save changes immediately after deleting
            });
        }

        builder.show();
    }

    private void removeTag(String tagName) {
        PhotoFile currentPhoto = photoList.get(currentIndex);
        List<Tag> tags = currentPhoto.getTags();
        Tag tagToRemove = null;

        for (Tag tag : tags) {
            if (tag.getTagName().equalsIgnoreCase(tagName)) {
                tagToRemove = tag;
                break;
            }
        }

        if (tagToRemove != null) {
            // Remove the tag from autocomplete if it's the last occurrence
            TagManager.removeTag(getApplicationContext(), tagToRemove.getTagName(), tagToRemove.getTagValue());

            // Remove the tag from the photo
            currentPhoto.removeTag(tagToRemove);
        }
    }

    private void removeTag(String tagName, String tagValue) {
        // This is used when changing tags - just decrease the count
        TagManager.removeTag(getApplicationContext(), tagName, tagValue);
    }

    private void updateTag(String tagName, String tagValue) {
        PhotoFile currentPhoto = photoList.get(currentIndex);

        // Remove existing tag with this name if it exists
        removeTag(tagName);

        // Add the new tag
        Tag newTag = new Tag(tagName, tagValue);
        currentPhoto.addTag(newTag);

        // Add to TagManager to update autocomplete lists
        TagManager.addTag(getApplicationContext(), tagName, tagValue);

        Log.d(TAG, "Added tag " + tagName + "=" + tagValue + " to photo at index " + currentIndex);
    }

    private void savePhotoChanges() {
        // Save the updated PhotoFile to the database
        Database.getInstance().save(getApplicationContext());
        Toast.makeText(this, "Tags saved", Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        // When exiting, pass back the entire updated photoList
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedPhotoList", new ArrayList<>(photoList));
        resultIntent.putExtra("albumModified", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}