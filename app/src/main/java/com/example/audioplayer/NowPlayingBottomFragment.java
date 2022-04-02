package com.example.audioplayer;

import static android.content.Context.MODE_PRIVATE;
import static com.example.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.example.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.example.audioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.audioplayer.MainActivity.ARTIST_NAME;
import static com.example.audioplayer.MainActivity.ARTIST_NAME_TO_FRAG;
import static com.example.audioplayer.MainActivity.PATH_TO_FRAG;
import static com.example.audioplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.example.audioplayer.MainActivity.SONG_NAME;
import static com.example.audioplayer.MainActivity.SONG_NAME_TO_FRAG;
import static com.example.audioplayer.MainActivity.musicFiles;
import static com.example.audioplayer.MusicService.MUSIC_FILE;
import static com.example.audioplayer.MusicService.MUSIC_LAST_PLAYED;
import static com.example.audioplayer.PlayerActivity.position;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.audioplayer.databinding.FragmentNowPlayingBottomBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;


public class NowPlayingBottomFragment extends Fragment implements ServiceConnection {

    ImageView nextbtn , prevBtn , albumart;
    TextView songName , artistName;
    static FloatingActionButton playpausebtn;
    ActionPlaying actionPlaying;
    MusicService musicService;
    PlayerActivity playerActivity;
    FragmentNowPlayingBottomBinding  binding ;

    public NowPlayingBottomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNowPlayingBottomBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        artistName = view.findViewById(R.id.song_artist_mini_player);
        songName = view.findViewById(R.id.song_name_mini_player);
        nextbtn = view.findViewById(R.id.skip_next_bottom);
        prevBtn = view.findViewById(R.id.skip_prev_bottom);
        albumart = view.findViewById(R.id.bottom_album_art);
        playpausebtn = view.findViewById(R.id.play_pause_btn_mini_bottom);

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(),PlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("class","Now Playing");
                requireContext().startActivity(intent);
            }
        });



        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "next", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    musicService.nextBtnClicked();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE,null);
                        String artist = preferences.getString(ARTIST_NAME,null);
                        String song_name = preferences.getString(SONG_NAME,null);
                        if (path != null)
                        {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_NAME_TO_FRAG = artist;
                            SONG_NAME_TO_FRAG = song_name;

                        }
                        else
                        {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_NAME_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if (SHOW_MINI_PLAYER) {
                            if (PATH_TO_FRAG != null) {
                                byte[] art =getAlbumArt(PATH_TO_FRAG);
                                if (art != null) {
                                    Glide.with(getContext())
                                            .load(art)
                                            .into(albumart);
                                }
                                else
                                {
                                    Glide.with(getContext())
                                            .load(R.drawable.music)
                                            .into(albumart);
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artistName.setText(ARTIST_NAME_TO_FRAG);
                            }
                        }
                    }
                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "prev", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    musicService.prevBtnClicked();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE,null);
                        String artist = preferences.getString(ARTIST_NAME,null);
                        String song_name = preferences.getString(SONG_NAME,null);
                        if (path != null)
                        {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_NAME_TO_FRAG = artist;
                            SONG_NAME_TO_FRAG = song_name;

                        }
                        else
                        {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_NAME_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if (SHOW_MINI_PLAYER) {
                            if (PATH_TO_FRAG != null) {
                                byte[] art =getAlbumArt(PATH_TO_FRAG);
                                if (art != null) {
                                    Glide.with(getContext())
                                            .load(art)
                                            .into(albumart);
                                }
                                else
                                {
                                    Glide.with(getContext())
                                            .load(R.drawable.music)
                                            .into(albumart);
                                }

                                if (actionPlaying != null){
                                    actionPlaying.prevBtnClicked();
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artistName.setText(ARTIST_NAME_TO_FRAG);
                            }
                            Intent intent = new Intent(getActivity(),PlayerActivity.class);
                            onReceive(getContext(),intent);
                        }
                    }
                }
            }
        });
        playpausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playpauseBtnClicked();
//                Toast.makeText(getContext(), "play", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    if (musicService.isPlaying()) {
                        playpausebtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    }
                    else {
//                        Toast.makeText(getContext(), "paus", Toast.LENGTH_SHORT).show();
                        playpausebtn.setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                }

            }
        });


        return  view;
    }

    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context,MusicService.class);
        playerActivity = new PlayerActivity();

        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                    break;
            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                byte[] art =getAlbumArt(PATH_TO_FRAG);
                if (art != null) {
                    Glide.with(getContext())
                            .load(art)
                            .into(albumart);
                }
                else
                {
                    Glide.with(getContext())
                            .load(R.drawable.music)
                            .into(albumart);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artistName.setText(ARTIST_NAME_TO_FRAG);
                Intent intent = new Intent(getContext(),MusicService.class);
                if (getContext() != null)
                {
                    getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] art =mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;

    }
}