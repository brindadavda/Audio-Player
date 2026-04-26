package com.example.audioplayer.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.audioplayer.data.AudioRepository
import com.example.audioplayer.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioUiState(
    val songs: List<Song> = emptyList(),
    val selectedSongId: Long? = null,
    val isPlaying: Boolean = false
)

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)
    private val player = ExoPlayer.Builder(application).build()

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newId = mediaItem?.mediaId?.toLongOrNull()
            _uiState.value = _uiState.value.copy(selectedSongId = newId)
        }
    }

    init {
        player.addListener(playerListener)
        viewModelScope.launch {
            val songs = repository.loadSongs()
            _uiState.value = _uiState.value.copy(songs = songs)
            setPlaylist(songs)
        }
    }

    private fun setPlaylist(songs: List<Song>) {
        val items = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(Uri.parse(song.contentUri))
                .build()
        }
        player.setMediaItems(items)
        player.prepare()
    }

    fun playSong(song: Song) {
        val index = _uiState.value.songs.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            player.seekTo(index, 0L)
            player.playWhenReady = true
            player.play()
            _uiState.value = _uiState.value.copy(selectedSongId = song.id)
        }
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun onCleared() {
        player.removeListener(playerListener)
        player.release()
        super.onCleared()
    }
}
