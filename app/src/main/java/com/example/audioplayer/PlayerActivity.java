package com.example.audioplayer;

import static com.example.audioplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.example.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.example.audioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.audioplayer.MainActivity.musicFiles;
import static com.example.audioplayer.MainActivity.repeatBoolean;
import static com.example.audioplayer.MainActivity.shuffleBoolean;
import static com.example.audioplayer.MusicAdapter.mFiles;
import static com.example.audioplayer.MusicService.mediaPlayer;
import static com.example.audioplayer.MusicService.mediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.audioplayer.databinding.ActivityPlayerBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements  ActionPlaying , ServiceConnection {

    public TextView song_name, artist_name, duration_played, duration_total;
    ImageView back_btn, menu_btn, cover_art, imageviewGradient, id_shuffle, id_prev, id_next, id_repeat;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    public static int position = -1;
    static public ArrayList<MusicFiles> listsongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    public static MusicService musicService;
    public static int time = 0;
    int durationTotal, mCurrentPosition;
    ActivityPlayerBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFulScreen();
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        initlizeView();
        getIntentMethod();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                onBackPressed();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    time = progress * 1000;
                    musicService.seekTo(progress * 1000);
                    musicService.showNotification(R.drawable.ic_baseline_pause_24, progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    duration_played.setText(formattedTime(mCurrentPosition));
                    duration_total.setText("-" + formattedTime(durationTotal - mCurrentPosition));
                    seekBar.setProgress(mCurrentPosition);
                    if (musicService.isPlaying() && musicService != null) {
                        musicService.showNotification(R.drawable.ic_baseline_pause_24, 1f);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });


        id_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    id_shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24);
                } else {
                    shuffleBoolean = true;
                    id_shuffle.setImageResource(R.drawable.ic_baseline_shuffle_on);
                }
            }
        });

        id_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    id_repeat.setImageResource(R.drawable.ic_baseline_repeat_off);
                } else {
                    repeatBoolean = true;
                    id_repeat.setImageResource(R.drawable.ic_baseline_repeat_on);
                }
            }
        });

    }

    private void setFulScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    public void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                id_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listsongs.size());
            }
            //else position will not change
            uri = Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_pause_24, 1f);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listsongs.size());
            }
            //else position will not change
            uri = Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24, 0f);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                id_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listsongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listsongs.size() - 1) : (position - 1));
            }
            //else position will not change
            uri = Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_pause_24, 1f);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            position = ((position - 1) < 0 ? (listsongs.size() - 1) : (position - 1));
            uri = Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24, 0f);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24, 0f);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause_24, 1f);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalnew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalnew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalnew;
        } else {
            return totalout;
        }

    }

    public void getIntentMethod() {
        position = getIntent().getIntExtra("position", position);
        String sender = getIntent().getStringExtra("sender");

        String player = getIntent().getStringExtra("class");

        if (musicService != null && player.equals("Now Playing")) {
            binding.durationPlayed.setText(formattedTime(musicService.getCurrentPosition()));
            binding.durationTotal.setText(formattedTime(musicService.getDuration()));
            binding.seekbar.setProgress(musicService.getCurrentPosition());
            binding.seekbar.setMax(musicService.getDuration());
        }

        if (sender != null && sender.equals("albumDetails")) {
            listsongs = albumFiles;
        }
        if (sender != null && sender.equals("music")){
            listsongs = mFiles; // import from main activity
        }
        if (listsongs != null && sender != null && (sender.equals("albumDetails") || sender.equals("music"))) {
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
            uri = Uri.parse(listsongs.get(position).getPath());
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("servicePosition", position);
            startService(intent);
        }



    }

    private void initlizeView() {

        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekbar);
        back_btn = findViewById(R.id.back_btn);
        menu_btn = findViewById(R.id.menu_btn);
        cover_art = findViewById(R.id.cover_art);
        imageviewGradient = findViewById(R.id.imageviewGradient);
        id_shuffle = findViewById(R.id.id_shuffle);
        id_prev = findViewById(R.id.id_prev);
        id_next = findViewById(R.id.id_next);
        id_repeat = findViewById(R.id.id_repeat);
    }

    public void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        durationTotal = Integer.parseInt(listsongs.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gradient = findViewById(R.id.imageviewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());

                    } else {
                        ImageView gradient = findViewById(R.id.imageviewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        } else {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(R.drawable.music)
                    .into(cover_art);

            ImageView gradient = findViewById(R.id.imageviewGradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
//        Toast.makeText(this, "Connected"+musicService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listsongs.get(position).getTitle());
        artist_name.setText(listsongs.get(position).getArtist());
        musicService.OnCompleted();
        musicService.showNotification(R.drawable.ic_baseline_pause_24, 1f);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

}