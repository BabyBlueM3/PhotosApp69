package com.group69.photosapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group69.photosapp.PhotoAdapter;
import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements PhotoAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private TextView albumTitleView;
    private TextView selectionInfoView;
    private LinearLayout emptyStateView;
    private Button btnOpen, btnRename, btnDelete, btnCreate;

    private String albumName;
    private List<PhotoFile> photoList = new ArrayList<>();
    private boolean isSelectionMode = true; // Always in selection mode now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Get album name from intent
        albumName = getIntent().getStringExtra("ALBUM_NAME");
        if (albumName == null) {
            albumName = "Album";
        }

        // Initialize UI elements
        initUI();

        // Load photos from the album
        loadPhotosFromAlbum();

        // Set up the RecyclerView
        setupRecyclerView();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initUI() {
        albumTitleView = findViewById(R.id.txt_album_title);
        albumTitleView.setText(albumName);

        recyclerView = findViewById(R.id.recycler_photos);
        selectionInfoView = findViewById(R.id.txt_selection_info);
        emptyStateView = findViewById(R.id.empty_state);

        btnOpen = findViewById(R.id.btn_view);
        btnRename = findViewById(R.id.btn_move);
        btnDelete = findViewById(R.id.btn_delete);
        btnCreate = findViewById(R.id.btn_upload);

        // Initially disable buttons that require selection
        updateButtonStates(false);
    }

    private void loadPhotosFromAlbum() {
        // TODO: Implement loading photos from storage based on album name
        // This is a placeholder - you'll need to implement actual photo loading
        
        // For demonstration, let's add some placeholder photos
        photoList = new ArrayList<>();
        
        // Add sample photos for demonstration
        // In a real app, you'd load these from storage
        photoList.add(new PhotoFile("/path/to/photo1.jpg", "Beach sunset"));
        photoList.add(new PhotoFile("/path/to/photo2.jpg", "Mountain view"));
        photoList.add(new PhotoFile("/path/to/photo3.jpg", "Family dinner"));
        // Add more photos as needed
        
        // Update UI based on whether photos exist
        if (photoList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        // Create grid layout with 4 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);

        // Set up adapter
        adapter = new PhotoAdapter(this, photoList);
        adapter.setOnItemClickListener(this);
        adapter.setSelectionMode(true); // Always in selection mode
        recyclerView.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        btnOpen.setOnClickListener(v -> {
            List<PhotoFile> selectedPhotos = adapter.getSelectedPhotos();
            if (selectedPhotos.size() == 1) {
                openPhotoViewer(selectedPhotos.get(0));
            } else {
                Toast.makeText(this, "Please select a photo to open", Toast.LENGTH_SHORT).show();
            }
        });

        btnRename.setOnClickListener(v -> {
            List<PhotoFile> selectedPhotos = adapter.getSelectedPhotos();
            if (selectedPhotos.size() == 1) {
                showRenameDialog(selectedPhotos);
            } else {
                Toast.makeText(this, "Please select a photo to rename", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            List<PhotoFile> selectedPhotos = adapter.getSelectedPhotos();
            if (selectedPhotos.size() == 1) {
                showDeleteConfirmationDialog(selectedPhotos);
            } else {
                Toast.makeText(this, "Please select a photo to delete", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreate.setOnClickListener(v -> {
            // Create new photo or album functionality
            // This could open camera or file picker
            Toast.makeText(this, "Create functionality to be implemented", Toast.LENGTH_SHORT).show();
        });
    }

    // Open a single photo in the photo viewer
    private void openPhotoViewer(PhotoFile photoFile) {
        // TODO: Implement opening photo in viewer activity
        Toast.makeText(this, "Opening: " + photoFile.getCaption(), Toast.LENGTH_SHORT).show();
        
        // Example: Intent to PhotoViewerActivity
        // Intent intent = new Intent(this, PhotoViewerActivity.class);
        // intent.putExtra("PHOTO_PATH", photoFile.getFilePath());
        // startActivity(intent);
    }

    // Show rename dialog
    private void showRenameDialog(List<PhotoFile> photos) {
        // TODO: Implement rename dialog
        Toast.makeText(this, "Rename dialog to be implemented", Toast.LENGTH_SHORT).show();
    }

    // Show delete confirmation dialog
    private void showDeleteConfirmationDialog(List<PhotoFile> photos) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement photo deletion
                    Toast.makeText(this, "Delete functionality to be implemented", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Update button states based on selection
    private void updateButtonStates(boolean hasSelection) {
        btnOpen.setEnabled(hasSelection);
        btnRename.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
        // Create button is always enabled
    }

    // PhotoAdapter.OnItemClickListener implementation
    @Override
    public void onItemClick(int position) {
        // Do nothing on item click since selection is handled in the adapter
    }

    @Override
    public void onSelectionChanged(int count) {
        selectionInfoView.setText(count > 0 ? "1 selected" : "0 selected");
        selectionInfoView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        updateButtonStates(count > 0);
    }
}