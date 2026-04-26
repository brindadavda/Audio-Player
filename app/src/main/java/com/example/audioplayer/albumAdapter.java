package com.example.audioplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class albumAdapter extends RecyclerView.Adapter<albumAdapter.MyHolder> {
    private Context mContexts;
    private ArrayList<MusicFiles> albumFiles;

    public albumAdapter(Context mContexts, ArrayList<MusicFiles> albumFiles) {
        this.mContexts = mContexts;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContexts).inflate(R.layout.album_item,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.album_name.setText(albumFiles.get(position).getAlbum());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null)
        {
            Glide.with(mContexts).asBitmap()
                    .load(image)
                    .into(holder.album_image);
        }
        else
        {
            Glide.with(mContexts)
                    .load(R.drawable.music)
                    .into(holder.album_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContexts,AlbumDetailsActivity.class);
                intent.putExtra("albumName",albumFiles.get(position).getAlbum());
                mContexts.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView album_image;
        TextView album_name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            album_image = itemView.findViewById(R.id.album_img);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }
    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] art =mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return art;
    }
}
