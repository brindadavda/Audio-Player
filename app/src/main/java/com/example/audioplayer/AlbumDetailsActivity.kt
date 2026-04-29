package com.example.audioplayer

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AlbumDetailsActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var albumPhoto: ImageView? = null
    private var albumName: String? = null
    private val albumSongs = arrayListOf<MusicFiles>()
    private var albumDetailsAdapter: AlbumDetailsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_details)

        recyclerView = findViewById(R.id.recyclerView1)
        albumPhoto = findViewById(R.id.album_photo)
        albumName = intent.getStringExtra("albumName")

        MainActivity.musicFiles?.forEach { music ->
            if (albumName == music.album) {
                albumSongs.add(music)
            }
        }

        if (albumSongs.isNotEmpty()) {
            val image = getAlbumArt(albumSongs[0].path)
            if (image != null) {
                Glide.with(this).load(image).into(albumPhoto!!)
            } else {
                Glide.with(this).load(R.drawable.music).into(albumPhoto!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (albumSongs.isNotEmpty()) {
            albumDetailsAdapter = AlbumDetailsAdapter(this, albumSongs)
            recyclerView?.adapter = albumDetailsAdapter
            recyclerView?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }
}
