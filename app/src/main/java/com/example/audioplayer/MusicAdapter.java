package com.example.audioplayer;

import static com.example.audioplayer.MainActivity.frag_bottom_player;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder>
{

    private Context mcontext;
    static ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context mcontext, ArrayList<MusicFiles> mFiles) {
        this.mcontext = mcontext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.music_items,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.file_name.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null)
        {
            Glide.with(mcontext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        }
        else
        {
            Glide.with(mcontext)
                    .load(R.drawable.music)
                    .into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext,PlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("sender","music");
                mcontext.startActivity(intent);
                frag_bottom_player.setVisibility(View.VISIBLE);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( final View v) {
                PopupMenu popupMenu = new PopupMenu(mcontext,v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) -> {
                    switch (item.getItemId())
                    {
                        case R.id.delete:
                            deleteFile(position,v);
                            break;
                    }
                    return true;
                });
            }
        });
    }

    private void deleteFile(int position, View v) {
        //this used to delete data related to file
        Uri contenturi = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,Long.parseLong(mFiles.get(position).getId()));
        File file = new File(mFiles.get(position).getPath());
        file.deleteOnExit();
        Log.d("hi",""+file.exists());
        boolean deleted = file.delete(); // delete your file
        Log.d("Error : ",""+file.delete());
        if (deleted) {
            mcontext.getContentResolver().delete(contenturi,null,null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG)
                    .show();
        }
        else
        {
            //may be file in sd card or api is more 19
            Snackbar.make(v, "File Can't Deleted", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView file_name;
        ImageView album_art,menuMore;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menu_more);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] art =mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return art;
    }

    @SuppressLint("NotifyDataSetChanged")
    void updateList(ArrayList<MusicFiles> musicFilesArrayList)
    {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
