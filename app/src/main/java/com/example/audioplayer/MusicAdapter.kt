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

class MusicAdapter(
    private val mcontext: Context,
    musicFiles: ArrayList<MusicFiles>
) : RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {

    companion object {
        @JvmField
        var mFiles: ArrayList<MusicFiles> = ArrayList()
    }

    init {
        mFiles = musicFiles
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mcontext).inflate(R.layout.music_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.fileName.text = mFiles[position].title
        val image = getAlbumArt(mFiles[position].path)
        if (image != null) {
            Glide.with(mcontext).asBitmap().load(image).into(holder.albumArt)
        } else {
            Glide.with(mcontext).load(R.drawable.music).into(holder.albumArt)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mcontext, PlayerActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("sender", "music")
            mcontext.startActivity(intent)
            MainActivity.frag_bottom_player?.visibility = View.VISIBLE
        }

        holder.menuMore.setOnClickListener { v ->
            val popupMenu = PopupMenu(mcontext, v)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete -> deleteFile(position, v)
                }
                true
            }
        }
    }

    private fun deleteFile(position: Int, v: View) {
        val contentUri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            mFiles[position].id.toLong()
        )
        val file = File(mFiles[position].path)
        file.deleteOnExit()
        Log.d("hi", "${file.exists()}")
        val deleted = file.delete()
        Log.d("Error : ", "${file.delete()}")
        if (deleted) {
            mcontext.contentResolver.delete(contentUri, null, null)
            mFiles.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, mFiles.size)
            Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(v, "File Can't Deleted", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = mFiles.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.music_file_name)
        val albumArt: ImageView = itemView.findViewById(R.id.music_img)
        val menuMore: ImageView = itemView.findViewById(R.id.menu_more)
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(musicFilesArrayList: ArrayList<MusicFiles>) {
        mFiles = ArrayList()
        mFiles.addAll(musicFilesArrayList)
        notifyDataSetChanged()
    }
}
