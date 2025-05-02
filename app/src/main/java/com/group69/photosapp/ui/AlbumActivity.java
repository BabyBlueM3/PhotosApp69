package com.group69.photosapp.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group69.photosapp.Album;
import com.group69.photosapp.Database;
import com.group69.photosapp.PhotoAdapter;
import com.group69.photosapp.PhotoData;
import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements PhotoAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private TextView albumTitleView;
    private LinearLayout emptyStateView;
    private Button btnView, btnMove, btnDelete, btnUpload;
    private Album currentAlbum;
    private static final int REQUEST_CODE_UPLOAD = 1001;

    private String albumName;
    private List<PhotoFile> photoList = new ArrayList<>();
    private boolean isSelectionMode = true; // Always in selection mode now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Get album name from intent
        albumName = getIntent().getStringExtra("ALBUM_NAME");

        // In your onCreate() or similar method
        Intent intent = getIntent();
        Album album = (Album) intent.getSerializableExtra("album");


        // Get the album object from PhotoData
        currentAlbum = PhotoData.getInstance().getAlbumByName(albumName);
        System.out.println(currentAlbum);
        // If album is found, set the title to the album's name
        if (currentAlbum != null) {
            albumName = currentAlbum.getName();  // Get the actual album name from the album object
        } else {
            albumName = "Album";  // Fallback in case the album is not found
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
        emptyStateView = findViewById(R.id.empty_state);

        btnView = findViewById(R.id.btn_view);
        btnMove = findViewById(R.id.btn_move);
        btnDelete = findViewById(R.id.btn_delete);
        btnUpload = findViewById(R.id.btn_upload);

        // Initially disable buttons that require selection
        updateButtonStates(false);
    }

    private void loadPhotosFromAlbum() {
        // Fetch the album using the passed name from HomeActivity
        String albumName = getIntent().getStringExtra("ALBUM_NAME");
        Log.d("AlbumActivity", "Opening album: " + albumName);

        if (albumName == null) {
            Toast.makeText(this, "No album name received", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the album from PhotoData by album name
        currentAlbum = PhotoData.getInstance().getAlbumByName(albumName);

        // If the album is not found, create it and add it to PhotoData
        if (currentAlbum == null) {
            currentAlbum = new Album(albumName);
            PhotoData.getInstance().addAlbum(currentAlbum);
        }

        // Now we can safely get the photos of the current album
        photoList = currentAlbum.getPhotos();

        // If it's the "Stock" album, load photos from the "stock" directory
        if (albumName.equalsIgnoreCase("Stock")) {
            // Ensure stock photos are not already added to the list
            File stockDir = new File(getFilesDir(), "stock");
            File[] photoFiles = stockDir.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
            });

            // Add stock photos to the list only if they are not already added
            if (photoFiles != null) {
                for (File file : photoFiles) {
                    String path = file.getAbsolutePath();
                    String caption = file.getName();
                    boolean exists = false;

                    // Check if the photo is already in the album to avoid duplicates
                    for (PhotoFile existingPhoto : photoList) {
                        if (existingPhoto.getCaption().equals(caption)) {
                            exists = true;
                            break;
                        }
                    }

                    // Only add the photo if it doesn't already exist in the list
                    if (!exists) {
                        photoList.add(new PhotoFile(path, caption));
                    }
                }
            }
        }

        Log.d("PhotoData", "Loaded photos: " + photoList.size());

        // Update UI based on whether the album has photos
        if (photoList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }

        // Set up the adapter and notify it with the photo list
        PhotoAdapter adapter = new PhotoAdapter(this, photoList);
        recyclerView.setAdapter(adapter);
        adapter.updatePhotoList(photoList);
        adapter.notifyDataSetChanged();
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

        btnView.setOnClickListener(v -> {
            List<PhotoFile> selectedPhotos = adapter.getSelectedPhotos();
            if (selectedPhotos.size() == 1) {
                PhotoFile photoFile = selectedPhotos.get(0);

                // Use the existing currentAlbum reference
                List<PhotoFile> photoList = currentAlbum.getPhotos();

                int index = photoList.indexOf(photoFile); // Find the position of the selected photo

                if (index != -1) {
                    Intent intent = new Intent(AlbumActivity.this, ViewActivity.class);
                    intent.putExtra("photoList", new ArrayList<>(photoList));
                    intent.putExtra("photoIndex", index);
                    intent.putExtra("photoFile", photoFile); // Send the selected photo as well
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Photo not found in album", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select a photo to view", Toast.LENGTH_SHORT).show();
            }
        });






        btnMove.setOnClickListener(v -> {
            List<PhotoFile> selectedPhotos = adapter.getSelectedPhotos();
            if (selectedPhotos.size() == 1) {
                showMoveDialog(selectedPhotos.get(0)); // single photo
            } else {
                Toast.makeText(this, "Please select a single photo to move", Toast.LENGTH_SHORT).show();
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

        btnUpload.setOnClickListener(v -> {
            // Create new photo or album functionality
            // This could open camera or file picker
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // allow multiple selection
            startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_CODE_UPLOAD);
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

    private void showMoveDialog(PhotoFile photoToMove) {
        // Replace with your actual album list
        List<Album> allAlbums = PhotoData.getInstance().getAlbums(); // or however you get them
        List<String> albumNames = new ArrayList<>();
        for (Album album : allAlbums) {
            System.out.println(album);
            // Exclude current album
            if (!album.getName().equals(currentAlbum.getName())) {
                albumNames.add(album.getName());
            }
        }

        if (albumNames.isEmpty()) {
            Toast.makeText(this, "No other albums available to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] albumsArray = albumNames.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Album to Move To");
        builder.setItems(albumsArray, (dialog, which) -> {
            String targetAlbumName = albumsArray[which];
            movePhotoToAlbum(photoToMove, targetAlbumName);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void movePhotoToAlbum(PhotoFile photo, String targetAlbumName) {
        List<Album> allAlbums = PhotoData.getInstance().getAlbums();
        Album targetAlbum = null;
        for (Album album : allAlbums) {
            if (album.getName().equals(targetAlbumName)) {
                targetAlbum = album;
                break;
            }
        }

        if (targetAlbum == null) {
            Toast.makeText(this, "Target album not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetAlbum.containsPhoto(photo)) {
            Toast.makeText(this, "Photo already exists in that album", Toast.LENGTH_SHORT).show();
            return;
        }

        currentAlbum.removePhoto(photo); // however you track current album
        targetAlbum.addPhoto(photo);
        PhotoData.getInstance().saveData(); // persist if needed

        Toast.makeText(this, "Photo moved to " + targetAlbumName, Toast.LENGTH_SHORT).show();

        // Update adapter
        adapter.updatePhotoList(currentAlbum.getPhotos());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPLOAD && resultCode == RESULT_OK) {
            if (data == null) return;

            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    addPhotoToAlbum(imageUri);
                }
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                addPhotoToAlbum(imageUri);
            }

            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }
    private void addPhotoToAlbum(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            String fileName = System.currentTimeMillis() + ".jpg"; // Unique name
            File photoFile = new File(getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(photoFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            PhotoFile newPhoto = new PhotoFile(photoFile.getAbsolutePath(), fileName);
            currentAlbum.addPhoto(newPhoto);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error uploading photo", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateEmptyState() {
        if (photoList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }




    // Update button states based on selection
    private void updateButtonStates(boolean hasSelection) {
        btnView.setEnabled(hasSelection);
        btnMove.setEnabled(hasSelection);
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
        // Just update button states based on selection count
        updateButtonStates(count > 0);
    }



}