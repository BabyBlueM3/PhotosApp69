package com.group69.photosapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;
import com.group69.photosapp.Tag;

import java.util.List;

public class ViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView personTagValue;
    private TextView locationTagValue;
    private Button editPersonButton;
    private Button editLocationButton;
    private Button slideshowLeft;
    private Button slideshowRight;

    // Reference to the PhotoFile object
    private PhotoFile photoFile;

    // Tag constants
    private static final String TAG_PERSON = "person";
    private static final String TAG_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

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
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("photoFile")) {
            photoFile = intent.getSerializableExtra("photoFile", PhotoFile.class);
            loadImage();
            updateTagsDisplay();
        } else {
            Toast.makeText(this, "No photo data received", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no photo data
        }
    }

    private void loadImage() {
        try {
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
            e.printStackTrace();
        }
    }

    private void updateTagsDisplay() {
        String personValue = findTagValue(TAG_PERSON);
        String locationValue = findTagValue(TAG_LOCATION);

        // Update UI with tag values
        personTagValue.setText(personValue.isEmpty() ? "No person tagged" : personValue);
        locationTagValue.setText(locationValue.isEmpty() ? "No location tagged" : locationValue);
    }

    private String findTagValue(String tagName) {
        List<Tag> tags = photoFile.getTags();
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
    }

    private void showEditTagDialog(String tagType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        boolean isPerson = tagType.equals(TAG_PERSON);
        String currentValue = isPerson ? findTagValue(TAG_PERSON) : findTagValue(TAG_LOCATION);

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
            if (newValue.isEmpty()) {
                // Remove tag if value is empty
                removeTag(tagType);
            } else {
                // Update or add the tag
                updateTag(tagType, newValue);
            }
            updateTagsDisplay();
            savePhotoChanges();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        if (!currentValue.isEmpty()) {
            // Add delete button if tag exists
            builder.setNeutralButton("Delete", (dialog, which) -> {
                removeTag(tagType);
                updateTagsDisplay();
                savePhotoChanges();
            });
        }

        builder.show();
    }

    private void removeTag(String tagName) {
        List<Tag> tags = photoFile.getTags();
        Tag tagToRemove = null;

        for (Tag tag : tags) {
            if (tag.getTagName().equalsIgnoreCase(tagName)) {
                tagToRemove = tag;
                break;
            }
        }

        if (tagToRemove != null) {
            photoFile.removeTag(tagToRemove);
        }
    }

    private void updateTag(String tagName, String tagValue) {
        // First remove existing tag with same name if exists
        removeTag(tagName);

        // Then add new tag
        Tag newTag = new Tag(tagName, tagValue);
        photoFile.addTag(newTag);
    }

    /**
     * Save the PhotoFile with updated tags
     * This is a placeholder for your implementation
     */
    private void savePhotoChanges() {
        // TODO: Implement saving the updated PhotoFile
        // This could involve your app's data storage mechanism

        Toast.makeText(this, "Tags saved", Toast.LENGTH_SHORT).show();
    }

    private void handleBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedPhotoFile", photoFile);
        setResult(RESULT_OK, resultIntent);
        finish(); // Replace super.onBackPressed()
    }
}
