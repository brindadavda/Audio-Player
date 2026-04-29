package com.example.audioplayer

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.io.File

class AlbumDetailsAdapter(
    private val mContexts: Context,
    albumFilesInput: ArrayList<MusicFiles>
) : RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder>() {

    companion object {
        @JvmField
        var albumFiles: ArrayList<MusicFiles> = ArrayList()
    }

    init {
        albumFiles = albumFilesInput
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContexts).inflate(R.layout.music_items, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.albumName.text = albumFiles[position].title
        val image = getAlbumArt(albumFiles[position].path)
        if (image != null) {
            Glide.with(mContexts).asBitmap().load(image).into(holder.albumImage)
        } else {
            Glide.with(mContexts).load(R.drawable.music).into(holder.albumImage)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContexts, PlayerActivity::class.java)
            intent.putExtra("sender", "albumDetails")
            intent.putExtra("position", position)
            mContexts.startActivity(intent)
            MainActivity.frag_bottom_player?.visibility = View.VISIBLE
        }

        holder.menuMore.setOnClickListener { v ->
            val popupMenu = PopupMenu(mContexts, v)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.delete) {
                    deleteFile(position, v)
                }
                true
            }
        }
    }

    private fun deleteFile(position: Int, v: View) {
        val contentUri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            albumFiles[position].id.toLong()
        )
        val file = File(albumFiles[position].path)
        file.deleteOnExit()
        Log.d("hi", "${file.exists()}")
        val deleted = file.delete()
        Log.d("Error : ", "${file.delete()}")
        if (deleted) {
            mContexts.contentResolver.delete(contentUri, null, null)
            albumFiles.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, albumFiles.size)
            Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(v, "File Can't Deleted", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = albumFiles.size

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumImage: ImageView = itemView.findViewById(R.id.music_img)
        val menuMore: ImageView = itemView.findViewById(R.id.menu_more)
        val albumName: TextView = itemView.findViewById(R.id.music_file_name)
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }
}
