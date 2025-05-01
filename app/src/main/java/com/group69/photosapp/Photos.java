package com.group69.photosapp;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class Photos extends AppCompatActivity {

    // We'll directly load and display the photos from the device in an album.
    private Album stockAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Reference the layout for the main activity

        // Create and load the stock album with photos
        stockAlbum = new Album("stock");
        loadStockPhotos(stockAlbum);

        // Optionally you could initialize UI components here to show the photos in the album
    }

    private void loadStockPhotos(Album album) {
        // Assuming your stock directory is within the app's file system
        String stockDirPath = getFilesDir().getAbsolutePath() + "/stock/";

        File stockDir = new File(stockDirPath);
        File[] photoFiles = stockDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });

        if (photoFiles != null) {
            for (File file : photoFiles) {
                String filePath = file.getAbsolutePath();
                String caption = file.getName();
                PhotoFile photo = new PhotoFile(filePath, caption);
                album.addPhoto(photo);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Handle any save functionality here if needed, but it's not necessary if you are no longer saving user data
    }
}
