package com.example.audioplayer

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class albumAdapter(
    private val mContexts: Context?,
    private val albumFiles: ArrayList<MusicFiles>
) : RecyclerView.Adapter<albumAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContexts).inflate(R.layout.album_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.albumName.text = albumFiles[position].album
        val image = getAlbumArt(albumFiles[position].path)
        if (image != null) {
            Glide.with(mContexts!!).asBitmap().load(image).into(holder.albumImage)
        } else {
            Glide.with(mContexts!!).load(R.drawable.music).into(holder.albumImage)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContexts, AlbumDetailsActivity::class.java)
            intent.putExtra("albumName", albumFiles[position].album)
            mContexts?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = albumFiles.size

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumImage: ImageView = itemView.findViewById(R.id.album_img)
        val albumName: TextView = itemView.findViewById(R.id.album_name)
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }
}
