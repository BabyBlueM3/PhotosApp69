package com.group69.photosapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group69.photosapp.PhotoFile;
import com.group69.photosapp.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    
    private List<PhotoFile> photoList;
    private Context context;
    private int selectedPosition = -1; // For single selection
    private boolean isSelectionMode = false;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onSelectionChanged(int count);
    }
    
    public PhotoAdapter(Context context, List<PhotoFile> photoList) {
        this.context = context;
        this.photoList = photoList != null ? photoList : new ArrayList<>();
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoFile photoFile = photoList.get(position);
        
        // Load the thumbnail image
        Bitmap thumbnail = getThumbnail(photoFile.getFilePath());
        if (thumbnail != null) {
            holder.imageView.setImageBitmap(thumbnail);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_photo);
        }
        
        // Set caption if exists
        if (photoFile.getCaption() != null && !photoFile.getCaption().isEmpty()) {
            holder.captionText.setText(photoFile.getCaption());
            holder.captionText.setVisibility(View.VISIBLE);
        } else {
            holder.captionText.setVisibility(View.GONE);
        }
        
        // Handle selection state
        boolean isSelected = (position == selectedPosition);
        holder.selectionOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public int getItemCount() {
        return photoList.size();
    }
    
    // Helper method to get thumbnail from file path
    private Bitmap getThumbnail(String filePath) {
        try {
            // Options to downsize the image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8; // Down-sample to 1/8 of original size
            
            return BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Toggle selection mode
    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) {
            selectedPosition = -1;
            if (listener != null) {
                listener.onSelectionChanged(0);
            }
        }
        notifyDataSetChanged();
    }
    
    // Select a single item
    public void selectItem(int position) {
        // If same position is selected, clear selection
        if (selectedPosition == position) {
            selectedPosition = -1;
            if (listener != null) {
                listener.onSelectionChanged(0);
            }
        } else {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            
            // Notify changes
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            
            if (listener != null) {
                listener.onSelectionChanged(1);
            }
        }
        notifyItemChanged(position);
    }
    
    // Get selected photo
    public List<PhotoFile> getSelectedPhotos() {
        List<PhotoFile> selected = new ArrayList<>();
        if (selectedPosition != -1) {
            selected.add(photoList.get(selectedPosition));
        }
        return selected;
    }
    
    // Clear selection
    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = -1;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
    }

    public void updatePhotos(List<PhotoFile> newPhotos) {
        this.photoList = newPhotos;
        notifyDataSetChanged();
    }

    // Update the data set
    public void updatePhotoList(List<PhotoFile> newPhotoList) {
        this.photoList = newPhotoList;
        selectedPosition = -1;
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
        notifyDataSetChanged();
    }
    
    // ViewHolder class
    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionText;
        View selectionOverlay;
        
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_photo);
            captionText = itemView.findViewById(R.id.txt_caption);
            selectionOverlay = itemView.findViewById(R.id.selection_overlay);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Always select on tap
                    selectItem(position);
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}