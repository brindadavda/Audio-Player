package com.example.audioplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SongsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView?.setHasFixedSize(true)

        MainActivity.musicFiles?.let { files ->
            if (files.isNotEmpty()) {
                musicAdapter = MusicAdapter(requireContext(), files)
                recyclerView?.adapter = musicAdapter
                recyclerView?.layoutManager =
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
        }

        return view
    }

    companion object {
        lateinit var musicAdapter: MusicAdapter
    }
}
