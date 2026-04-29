package com.example.audioplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.SearchView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import java.util.Locale

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val mySortPref = "SortOrder"
    private var musicService: MusicService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                onMiniPlayerClick = { startActivity(Intent(applicationContext, PlayerActivity::class.java)) }
            )
        }
        permission()
    }

    @Composable
    private fun MainScreen(onMiniPlayerClick: () -> Unit) {
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { context ->
                    TabLayout(context).apply {
                        id = R.id.tab_layout1
                        setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                        setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.white))
                        setTabTextColors(
                            ContextCompat.getColor(context, R.color.white),
                            ContextCompat.getColor(context, R.color.black)
                        )
                        setSelectedTabIndicatorHeight(resources.getDimensionPixelSize(R.dimen.tab_indicator_height))
                    }
                }
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context -> ViewPager(context).apply { id = R.id.viewpager } }
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { context ->
                    FrameLayout(context).apply {
                        id = R.id.frag_bottom_player
                        visibility = View.GONE
                        setOnClickListener { onMiniPlayerClick() }
                    }
                },
                update = { frameLayout ->
                    frag_bottom_player = frameLayout
                    attachBottomFragmentIfNeeded(frameLayout.id)
                }
            )
        }
    }

    private fun attachBottomFragmentIfNeeded(containerId: Int) {
        val existing = supportFragmentManager.findFragmentByTag("now_playing_bottom")
        if (existing == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerId, NowPlayingBottomFragment(), "now_playing_bottom")
                .commitNowAllowingStateLoss()
        }
    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        } else {
            musicFiles = getAllAudio(this)
            initViewPager()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                musicFiles = getAllAudio(this)
                initViewPager()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            }
        }
    }

    private fun initViewPager() {
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout1)
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragments(SongsFragment(), "Songs")
        adapter.addFragments(AlbumFragment(), "Albums")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun getAllAudio(context: Context): ArrayList<MusicFiles> {
        val preferences = getSharedPreferences(mySortPref, MODE_PRIVATE)
        val sortOrder = preferences.getString("sorting", "sortByName")
        val order = when (sortOrder) {
            "sortByDate" -> MediaStore.MediaColumns.DATE_ADDED + " ASC"
            "sortBySize" -> MediaStore.MediaColumns.SIZE + " DESC"
            else -> MediaStore.MediaColumns.DISPLAY_NAME + " ASC"
        }

        val duplicate = arrayListOf<String>()
        albums.clear()
        val tempArrayList = arrayListOf<MusicFiles>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID
        )

        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, order)
        cursor?.use {
            while (it.moveToNext()) {
                val album = it.getString(0)
                val title = it.getString(1)
                val duration = it.getString(2)
                val path = it.getString(3)
                val artist = it.getString(4)
                val id = it.getString(5)

                val musicFile = MusicFiles(path, title, artist, album, duration, id)
                tempArrayList.add(musicFile)
                if (!duplicate.contains(album)) {
                    albums.add(musicFile)
                    duplicate.add(album)
                }
            }
        }

        return tempArrayList
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        val menuItem = menu.findItem(R.id.search_option)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        val userInput = newText.lowercase(Locale.getDefault())
        val myFiles = arrayListOf<MusicFiles>()

        musicFiles?.forEach { song ->
            if (song.title.lowercase(Locale.getDefault()).contains(userInput)) {
                myFiles.add(song)
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val editor = getSharedPreferences(mySortPref, MODE_PRIVATE).edit()
        when (item.itemId) {
            R.id.sort_by_name -> {
                editor.putString("sorting", "sortByName")
                editor.apply()
                recreate()
            }

            R.id.sort_by_date -> {
                editor.putString("sorting", "sortByDate")
                editor.apply()
                recreate()
            }

            R.id.sort_by_size -> {
                editor.putString("sorting", "sortBySize")
                editor.apply()
                recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
        val path = preferences.getString(MUSIC_FILE, null)
        val artist = preferences.getString(ARTIST_NAME, null)
        val songName = preferences.getString(SONG_NAME, null)
        if (path != null) {
            SHOW_MINI_PLAYER = true
            PATH_TO_FRAG = path
            ARTIST_NAME_TO_FRAG = artist
            SONG_NAME_TO_FRAG = songName
        } else {
            SHOW_MINI_PLAYER = false
            PATH_TO_FRAG = null
            ARTIST_NAME_TO_FRAG = null
            SONG_NAME_TO_FRAG = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PlayerActivity.musicService != null && !PlayerActivity.musicService.isPlaying) {
            PlayerActivity.musicService.stopForeground(true)
            PlayerActivity.musicService.release()
            PlayerActivity.musicService = null
            System.exit(0)
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments = arrayListOf<Fragment>()
        private val titles = arrayListOf<String>()

        fun addFragments(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence = titles[position]
    }

    companion object {
        const val REQUEST_CODE = 1
        @JvmField
        var musicFiles: ArrayList<MusicFiles>? = null
        @JvmField
        var shuffleBoolean = false
        @JvmField
        var repeatBoolean = false
        @JvmField
        var albums: ArrayList<MusicFiles> = ArrayList()

        const val MUSIC_LAST_PLAYED = "LAST_PLAYED"
        const val MUSIC_FILE = "STORED_MUSIC"
        const val ARTIST_NAME = "ARTIST_NAME"
        const val SONG_NAME = "SONG_NAME"

        @JvmField
        var SHOW_MINI_PLAYER = false

        @JvmField
        var PATH_TO_FRAG: String? = null

        @JvmField
        var SONG_NAME_TO_FRAG: String? = null

        @JvmField
        var ARTIST_NAME_TO_FRAG: String? = null

        @JvmField
        var frag_bottom_player: FrameLayout? = null
    }
}
