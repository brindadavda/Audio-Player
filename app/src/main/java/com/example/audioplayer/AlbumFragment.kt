package com.example.audioplayer

import android.content.Intent
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment

class AlbumFragment : Fragment() {
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme {
                AlbumScreen(MainActivity.albums)
            }
        }
    }

    @Composable
    private fun AlbumScreen(albums: List<MusicFiles>) {
        val context = requireContext()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(context, AlbumDetailsActivity::class.java)
                            intent.putExtra("albumName", album.album)
                            context.startActivity(intent)
                        }
                ) {
                    AlbumArt(album.path)
                    Text(text = album.album, style = MaterialTheme.typography.titleMedium)
                    Text(text = album.artist, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    @Composable
    private fun AlbumArt(path: String) {
        val image = remember(path) { getAlbumArt(path) }
        if (image != null) {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(image, 0, image.size)
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(150.dp))
        } else {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.music),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
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
}
