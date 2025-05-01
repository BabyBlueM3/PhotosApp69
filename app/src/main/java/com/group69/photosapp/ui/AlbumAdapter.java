package com.group69.photosapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.group69.photosapp.Album;
import com.group69.photosapp.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context context;
    private List<Album> albums;

    public AlbumAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.albumName.setText(album.getName());

        // Update the visual state based on selection
        if (album.isSelected()) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
            holder.albumName.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.albumName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        // Set click listener for selection
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle selection
                boolean isSelected = !album.isSelected();

                // Deselect all other albums
                for (Album a : albums) {
                    a.setSelected(false);
                }

                // Select this album if it was clicked to be selected
                album.setSelected(isSelected);

                // Refresh the adapter
                notifyDataSetChanged();

                // Update button states in the activity
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).updateButtonStates();
                }
            }
        });

        // In a real app, you would load the album cover image here
        // For example, using Glide or Picasso
        // Glide.with(context).load(album.getCoverImageUrl()).into(holder.albumCover);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums;
        notifyDataSetChanged();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView albumCover;
        TextView albumName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            albumCover = itemView.findViewById(R.id.album_cover);
            albumName = itemView.findViewById(R.id.album_name);
        }
    }
}

