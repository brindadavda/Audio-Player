package com.example.audioplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AlbumFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var albumAdapter: albumAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView?.setHasFixedSize(true)

        if (MainActivity.albums.isNotEmpty()) {
            albumAdapter = albumAdapter(context, MainActivity.albums)
            recyclerView?.adapter = albumAdapter
            recyclerView?.layoutManager = GridLayoutManager(context, 2)
        }

        return view
    }
}
