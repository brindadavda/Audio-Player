package com.example.audioplayer

import android.content.ContentUris
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import java.io.File

class SongsFragment : Fragment() {

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme {
                SongsScreen()
            }
        }
    }

    @Composable
    private fun SongsScreen() {
        val context = requireContext()
        val songs = remember { mutableStateListOf<MusicFiles>().apply { addAll(MainActivity.musicFiles ?: emptyList()) } }
        remember {
            allSongs.clear()
            allSongs.addAll(MainActivity.musicFiles ?: emptyList())
            onSearchResults = { filtered ->
                songs.clear()
                songs.addAll(filtered)
            }
            true
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(songs, key = { _, item -> item.id }) { index, song ->
                var menuExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(context, PlayerActivity::class.java)
                            intent.putExtra("position", index)
                            intent.putExtra("sender", "music")
                            context.startActivity(intent)
                            MainActivity.frag_bottom_player?.visibility = android.view.View.VISIBLE
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumArt(song.path)
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(text = song.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = song.artist, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { menuExpanded = true }) { Text("⋮") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                deleteSong(context, song)
                                songs.remove(song)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AlbumArt(path: String) {
        val image = remember(path) { getAlbumArt(path) }
        if (image != null) {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(image, 0, image.size)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        } else {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.music),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        }
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

    private fun deleteSong(context: android.content.Context, song: MusicFiles) {
        val contentUri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id.toLong()
        )
        val file = File(song.path)
        val deleted = file.delete()
        if (deleted) {
            context.contentResolver.delete(contentUri, null, null)
            Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "File can't be deleted", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        private val allSongs = mutableListOf<MusicFiles>()
        private var onSearchResults: ((List<MusicFiles>) -> Unit)? = null

        fun applySearch(filtered: List<MusicFiles>) {
            onSearchResults?.invoke(filtered)
        }
    }

}
