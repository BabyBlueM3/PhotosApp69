package com.group69.photosapp.ui;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import com.group69.photosapp.Album;
import com.group69.photosapp.Database;
import com.group69.photosapp.PhotoData;
import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;
import com.group69.photosapp.Tag;
import com.group69.photosapp.TagManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView albumsRecyclerView;
    private AlbumAdapter albumAdapter;
    private ArrayList<Album> albumList;
    private Button btnOpen, btnRename, btnDelete, btnCreate, btnSearch;
    private AutoCompleteTextView searchLocation, searchPerson;
    private Spinner searchOperator;

    // Tag data for autocomplete suggestions
    public ArrayList<String> locationTags;

    public ArrayList<String> personTags;

    // Keep track of our adapters for autocomplete to update them
    private ArrayAdapter<String> locationAdapter;
    private ArrayAdapter<String> personAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the tag lists and adapters when returning to this activity
        refreshAutocompleteSuggestions();
    }

    /**
     * Refreshes the autocomplete suggestions by updating the tag lists and adapters
     */
    private void refreshAutocompleteSuggestions() {
        // Refresh the tag lists from the database
        locationTags = Database.getInstance().getLocationTags();
        personTags = Database.getInstance().getPersonTags();

        Log.d("HomeActivity", "Refreshed tags - Locations: " + locationTags.size() + ", Persons: " + personTags.size());

        // Update the adapters with the refreshed lists
        if (locationAdapter != null) {
            locationAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, locationTags);
            searchLocation.setAdapter(locationAdapter);
        }

        if (personAdapter != null) {
            personAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, personTags);
            searchPerson.setAdapter(personAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Load the database from storage (only once)
        Database.load(getApplicationContext());

        // Initialize tag counts for autocomplete
        TagManager.initializeTagCounts();

        // Initialize the RecyclerView
        albumsRecyclerView = findViewById(R.id.albums_recycler_view);

        // Set layout manager (2 columns grid)
        albumsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (albumList == null) {
            albumList = new ArrayList<>();
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

        // Load the autocomplete lists
        locationTags = Database.getInstance().getLocationTags();
        personTags = Database.getInstance().getPersonTags();

        // Initialize and set adapter
        albumAdapter = new AlbumAdapter(this, albumList);
        albumsRecyclerView.setAdapter(albumAdapter);

        // Assuming albumList is already populated
        PhotoData.getInstance().setAlbums(albumList);

        // Load the albums into the album list
        loadAlbums();

        // Initialize search components
        initializeSearchComponents();

        // Initialize buttons
        initializeButtons();

        // Update the button states initially
        updateButtonStates();
    }

    private void initializeSearchComponents() {
        // Find views by ID
        searchLocation = findViewById(R.id.search_location);
        searchPerson = findViewById(R.id.search_person);
        searchOperator = findViewById(R.id.search_operator);
        btnSearch = findViewById(R.id.btn_search);

        // Set up autocomplete adapters with dropdown suggestions
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, locationTags);
        searchLocation.setAdapter(locationAdapter);
        searchLocation.setThreshold(1); // Show suggestions after typing 1 character

        ArrayAdapter<String> personAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, personTags);
        searchPerson.setAdapter(personAdapter);
        searchPerson.setThreshold(1); // Show suggestions after typing 1 character

        // Set up dropdown for AND/OR operator
        ArrayAdapter<CharSequence> operatorAdapter = ArrayAdapter.createFromResource(
                this, R.array.search_operators, android.R.layout.simple_spinner_item);
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchOperator.setAdapter(operatorAdapter);

        // Set up text changed listeners for filtering suggestions
        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocationSuggestions(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchPerson.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPersonSuggestions(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up search button click listener
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void filterLocationSuggestions(String query) {
        if (query.isEmpty()) {
            return;
        }

        List<String> filteredSuggestions = new ArrayList<>();
        for (String location : locationTags) {
            if (location.toLowerCase().startsWith(query)) {
                filteredSuggestions.add(location);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, filteredSuggestions);
        searchLocation.setAdapter(adapter);

        if (query.length() > 0 && !filteredSuggestions.isEmpty()) {
            searchLocation.showDropDown();
        }
    }

    private void filterPersonSuggestions(String query) {
        if (query.isEmpty()) {
            return;
        }

        List<String> filteredSuggestions = new ArrayList<>();
        for (String person : personTags) {
            if (person.toLowerCase().startsWith(query)) {
                filteredSuggestions.add(person);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, filteredSuggestions);
        searchPerson.setAdapter(adapter);

        if (query.length() > 0 && !filteredSuggestions.isEmpty()) {
            searchPerson.showDropDown();
        }
    }

    private void performSearch() {
        String locationQuery = searchLocation.getText().toString().trim();
        String personQuery = searchPerson.getText().toString().trim();
        boolean isAndOperator = searchOperator.getSelectedItem().toString().equals("AND");

        // Check if at least one search field is filled
        if (locationQuery.isEmpty() && personQuery.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search term", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find photos matching the search criteria
        List<PhotoFile> matchedPhotos = searchPhotos(locationQuery, personQuery, isAndOperator);

        if (matchedPhotos.isEmpty()) {
            Toast.makeText(this, "No photos found matching your search", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove any previous temporary albums
        for (Album album : new ArrayList<>(PhotoData.getInstance().getAlbums())) {
            if (album.isTemporary()) {
                PhotoData.getInstance().removeAlbum(album);
            }
        }

        // Create a temporary album with the search results
        String searchTitle = buildSearchTitle(locationQuery, personQuery, isAndOperator);
        Album searchResultsAlbum = new Album(searchTitle);
        searchResultsAlbum.setTemporary(true);

        // Add matched photos to temporary album
        for (PhotoFile photo : matchedPhotos) {
            searchResultsAlbum.addPhoto(photo);
        }

        // Add the temporary album to PhotoData
        PhotoData.getInstance().addAlbum(searchResultsAlbum);

        // Launch AlbumActivity with the temp album
        Intent intent = new Intent(HomeActivity.this, AlbumActivity.class);
        intent.putExtra("ALBUM_NAME", searchResultsAlbum.getName());
        intent.putExtra("IS_TEMP_ALBUM", true);
        startActivity(intent);

        // Clear search fields after search is performed
        searchLocation.setText("");
        searchPerson.setText("");
    }

    private String buildSearchTitle(String locationQuery, String personQuery, boolean isAndOperator) {
        StringBuilder title = new StringBuilder("Search Results: ");

        if (!locationQuery.isEmpty() && !personQuery.isEmpty()) {
            title.append("Location:").append(locationQuery)
                    .append(" ").append(isAndOperator ? "AND" : "OR")
                    .append(" Person:").append(personQuery);
        } else if (!locationQuery.isEmpty()) {
            title.append("Location:").append(locationQuery);
        } else {
            title.append("Person:").append(personQuery);
        }

        return title.toString();
    }

    private List<PhotoFile> searchPhotos(String locationQuery, String personQuery, boolean isAndOperator) {
        Set<PhotoFile> resultSet = new HashSet<>();

        // Search through all albums for matching photos
        for (Album album : PhotoData.getInstance().getAlbums()) {
            // Skip temporary albums
            if (album.isTemporary()) {
                continue;
            }

            for (PhotoFile photo : album.getPhotos()) {
                // If both fields are filled, apply AND/OR logic
                if (!locationQuery.isEmpty() && !personQuery.isEmpty()) {
                    boolean locationMatch = hasLocationTag(photo, locationQuery);
                    boolean personMatch = hasPersonTag(photo, personQuery);

                    if ((isAndOperator && locationMatch && personMatch) ||
                            (!isAndOperator && (locationMatch || personMatch))) {
                        resultSet.add(photo);
                    }
                }
                // If only location is filled, only check location
                else if (!locationQuery.isEmpty()) {
                    if (hasLocationTag(photo, locationQuery)) {
                        resultSet.add(photo);
                    }
                }
                // If only person is filled, only check person
                else if (!personQuery.isEmpty()) {
                    if (hasPersonTag(photo, personQuery)) {
                        resultSet.add(photo);
                    }
                }
            }
        }

        return new ArrayList<>(resultSet);
    }

    private boolean hasLocationTag(PhotoFile photo, String location) {
        // Check if the photo has the location tag
        for (Tag tag : photo.getTags()) {
            if (tag.getTagName().equals("location") &&
                    tag.getTagValue().toLowerCase().contains(location.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPersonTag(PhotoFile photo, String person) {
        // Check if the photo has the person tag
        for (Tag tag : photo.getTags()) {
            if (tag.getTagName().equals("person") &&
                    tag.getTagValue().toLowerCase().contains(person.toLowerCase())) {
                return true;
            }
        }
        return false;
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
                Album stockAlbum = null;

                // Check if the stock album already exists in the album list
                for (Album album : albumList) {
                    if (album.getName().equals("Stock")) {
                        stockAlbum = album;
                        break;
                    }
                }

                // If stock album already exists, skip loading
                if (stockAlbum != null) {
                    return;
                }

                // Stock album doesn't exist — create and populate it
                stockAlbum = new Album("Stock");
                albumList.add(stockAlbum);

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

                        // Only add the photo if it doesn't already exist in the album
                        if (!stockAlbum.getPhotos().contains(photo)) {
                            stockAlbum.addPhoto(photo);
                        }
                    }
                }

                Log.d("AlbumListActivity", "Album List after load: " + albumList.toString());

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

    public void addAlbumToDatabase(Album album) {
        Database.getInstance().addAlbum(album);  // Add album to Database
        Database.getInstance().save(getApplicationContext());  // Save the updated database
    }

    public void removeAlbumFromDatabase(Album album) {
        Database.getInstance().removeAlbum(album);  // Remove album from Database
        Database.getInstance().save(getApplicationContext());  // Save the updated database
    }
}