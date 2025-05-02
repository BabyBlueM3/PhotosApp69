package com.group69.photosapp.ui;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.content.Intent;


import com.group69.photosapp.Album;
import com.group69.photosapp.Database;
import com.group69.photosapp.PhotoData;
import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView albumsRecyclerView;
    private AlbumAdapter albumAdapter;
    private List<Album> albumList;
    private Button btnOpen, btnRename, btnDelete, btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Load the database from storage (only once)
        Database.load(getApplicationContext());

        // Initialize the RecyclerView
        albumsRecyclerView = findViewById(R.id.albums_recycler_view);

        // Set layout manager (2 columns grid)
        albumsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize album list
        albumList = new ArrayList<>();

        // Add sample albums (if they aren't already in the database or albumList)
        if (albumList.isEmpty()) {
            albumList.add(new Album("Vacation 2024"));
            albumList.add(new Album("Family Photos"));
            albumList.add(new Album("Nature"));
        }

        // Access the albums from the database (if any)
        ArrayList<Album> storedAlbums = Database.getInstance().getAlbums();
        if (storedAlbums != null) {
            // Avoid duplicates, only add albums that are not already in the list
            for (Album album : storedAlbums) {
                if (!albumList.contains(album)) {
                    albumList.add(album);
                }
            }
        }

        // Initialize and set adapter
        albumAdapter = new AlbumAdapter(this, albumList);
        albumsRecyclerView.setAdapter(albumAdapter);

        // Assuming albumList is already populated
        PhotoData.getInstance().setAlbums(albumList);

        // Copy stock photos to internal storage if necessary
        copyStockPhotosIfNeeded();

        // Load the albums into the album list
        loadAlbums();

        // Setup search functionality
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAlbums(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAlbums(newText);
                return true;
            }
        });

        // Initialize buttons
        initializeButtons();

        // Update the button states initially
        updateButtonStates();
    }


    private void initializeButtons() {
        btnOpen = findViewById(R.id.btn_open);
        btnRename = findViewById(R.id.btn_rename);
        btnDelete = findViewById(R.id.btn_delete);
        btnCreate = findViewById(R.id.btn_create);

        btnOpen.setOnClickListener(v -> {
            for (Album album : albumList) {
                if (album.isSelected()) {
                    Intent intent = new Intent(HomeActivity.this, AlbumActivity.class);
                    intent.putExtra("ALBUM_NAME", album.getName()); // <-- pass selected album
                    startActivity(intent);
                    break;
                }
            }
        });


        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Album album : albumList) {
                    if (album.isSelected()) {
                        // Create input dialog to rename
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Rename Album");

                        final EditText input = new EditText(HomeActivity.this);
                        input.setText(album.getName());
                        builder.setView(input);

                        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = input.getText().toString().trim();
                                if (!newName.isEmpty()) {
                                    album.setName(newName);
                                    albumAdapter.notifyDataSetChanged();
                                    Toast.makeText(HomeActivity.this, "Album renamed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", null);
                        builder.show();

                        break; // Only rename first selected album
                    }
                }
            }
        });


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Album album : new ArrayList<>(albumList)) {
                    if (album.isSelected()) {
                        new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("Delete Album")
                                .setMessage("Are you sure you want to delete the album \"" + album.getName() + "\"?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        albumList.remove(album);
                                        albumAdapter.updateAlbums(albumList);
                                        updateButtonStates();

                                        // Remove album from the database
                                        removeAlbumFromDatabase(album);

                                        Toast.makeText(HomeActivity.this, "Album deleted", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();

                        break; // Delete only the first selected album
                    }
                }
            }
        });


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Create New Album");

                final EditText input = new EditText(HomeActivity.this);
                input.setHint("Enter album name");
                builder.setView(input);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            boolean exists = false;
                            for (Album album : albumList) {
                                if (album.getName().equalsIgnoreCase(newName)) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (exists) {
                                Toast.makeText(HomeActivity.this, "Album name already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                Album newAlbum = new Album(newName);
                                albumList.add(newAlbum);
                                albumAdapter.updateAlbums(albumList);
                                updateButtonStates();

                                // Add album to the database
                                addAlbumToDatabase(newAlbum);

                                Toast.makeText(HomeActivity.this, "Album created", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });

    }

    // TODO change this to be search images rather than filter albums
    private void filterAlbums(String query) {
        // Filter albums based on search query
        // This is just a placeholder implementation
        List<Album> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(albumList);
        } else {
            String lowerCaseQuery = query.toLowerCase();

            for (Album album : albumList) {
                if (album.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(album);
                }
            }
        }

        albumAdapter.updateAlbums(filteredList);
        updateButtonStates();
    }

    /**
     * Updates the button states based on album selection
     */
    public void updateButtonStates() {
        boolean anySelected = false;

        // Check if any album is selected
        for (Album album : albumList) {
            if (album.isSelected()) {
                anySelected = true;
                break;
            }
        }

        // Enable/disable the buttons that require a selection
        btnOpen.setEnabled(anySelected);
        btnRename.setEnabled(anySelected);
        btnDelete.setEnabled(anySelected);
        // Create button is always enabled as it doesn't require selection
    }

    private void loadAlbums() {
        try {
            File stockDir = new File(getFilesDir(), "stock");

            // Check if the stock directory exists
            if (stockDir.exists() && stockDir.isDirectory()) {
                Album stockAlbum = new Album("Stock");

                // Get all photo files in the stock directory
                File[] photoFiles = stockDir.listFiles((dir, name) -> {
                    String lower = name.toLowerCase();
                    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
                });

                if (photoFiles != null) {
                    for (File file : photoFiles) {
                        String filePath = file.getAbsolutePath();
                        String caption = file.getName();
                        PhotoFile photo = new PhotoFile(filePath, caption);
                        stockAlbum.addPhoto(photo);
                        System.out.println(photo);
                    }
                }

                albumList.add(stockAlbum);  // Add the stock album to the list
                Log.d("AlbumListActivity", "Album List: " + albumList.toString());

            } else {
                Log.e("AlbumListActivity", "Stock directory not found or not a directory");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading albums", Toast.LENGTH_SHORT).show();
        }

        // Notify the adapter that the data has changed
        Log.d("AlbumListActivity", "Album list size before notifyDataSetChanged: " + albumList.size());

        albumAdapter.notifyDataSetChanged();
    }

    private void copyStockPhotosIfNeeded() {
        File stockDir = new File(getFilesDir(), "stock");
        if (!stockDir.exists()) {
            stockDir.mkdirs(); // Create directory if it doesn't exist
            try {
                AssetManager assetManager = getAssets();
                String[] photos = assetManager.list("stock"); // List files in assets/stock
                if (photos != null) {
                    for (String filename : photos) {
                        InputStream in = assetManager.open("stock/" + filename);
                        File outFile = new File(stockDir, filename);
                        OutputStream out = new FileOutputStream(outFile);

                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }

                        in.close();
                        out.flush();
                        out.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to copy stock photos", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void addAlbumToDatabase(Album album) {
        System.out.println("fuck");
        Database.getInstance().addAlbum(album);  // Add album to Database
        Database.getInstance().save(getApplicationContext());  // Save the updated database
    }

    public void removeAlbumFromDatabase(Album album) {
        Database.getInstance().removeAlbum(album);  // Remove album from Database
        Database.getInstance().save(getApplicationContext());  // Save the updated database
    }
}