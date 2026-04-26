package com.example.audioplayer;

import static com.example.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.example.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.example.audioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.audioplayer.PlayerActivity.listsongs;
import static com.example.audioplayer.PlayerActivity.musicService;
import static com.example.audioplayer.PlayerActivity.time;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.style.UpdateLayout;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MyBinder();
    static MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    static MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String SONG_NAME = "SONG_NAME";
    int max_progress = 100;
    int current_progress = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind","Method");
        return mBinder;
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1)
        {
            playMedia(myPosition);
        }
        if (actionName != null)
        {
            switch (actionName)
            {
                case "playPause":
//                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    playpauseBtnClicked();
                    break;
                case "next":
                    nextBtnClicked();
//                    Toast.makeText(this, "next", Toast.LENGTH_SHORT).show();
                    break;
                case "previous":
                   prevBtnClicked();
//                    Toast.makeText(this, "previous", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int Startposition) {
        musicFiles = listsongs;
        position = Startposition;
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start()
    {
        mediaPlayer.start();
    }

    boolean isPlaying()
    {
         return mediaPlayer.isPlaying();
    }

    void stop()
    {
        mediaPlayer.stop();
    }

    void release()
    {
        mediaPlayer.release();
    }

    int getDuration()
    {
       return mediaPlayer.getDuration();
    }

    void seekTo(int position)
    {
        mediaPlayer.seekTo(position);
    }

    void createMediaPlayer(int positionInner)
    {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED , MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE,uri.toString());
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.putString(ARTIST_NAME,musicFiles.get(position).getArtist());
        editor.apply();


        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }

    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null){
            actionPlaying.nextBtnClicked();
            if (mediaPlayer != null)
            {
                createMediaPlayer(position);
                start();
                OnCompleted();
            }
        }

    }

    void setCallBack(ActionPlaying actionPlaying)
    {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int PlayPauseBtn,float playbackspeed)
    {

        Intent intent = new Intent(this , PlayerActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("class","Now Playing");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);


        Intent prevIntent =new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this , 0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent =new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this , 0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent =new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this , 0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null)
        {
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else
        {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music);
        }


        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(PlayPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"Previous" , prevPending)
                .addAction(PlayPauseBtn,"Pause" , pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"Next" , nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setProgress(max_progress,current_progress,false)
                .setContentIntent(resultPendingIntent)
                .setProgress(getDuration(),getCurrentPosition(),false)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration())
                    .build());

            mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING , getCurrentPosition(), playbackspeed )
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO )
                    .setActions( PlaybackStateCompat.ACTION_PLAY |
                            PlaybackStateCompat.ACTION_PAUSE |
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                           PlaybackStateCompat.ACTION_SEEK_TO |
                            PlaybackStateCompat.ACTION_PLAY_PAUSE )
//                    .setState(
//                            PlaybackStateCompat.STATE_PLAYING,
//                            getCurrentPosition(),
//                            1f,
//                            SystemClock.elapsedRealtime()
//                    )


                    .build());
        }


        startForeground(2,notification);

    }
    

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] art =mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return art;
    }

    public void playpauseBtnClicked(){
        if (actionPlaying != null)
        {
            Log.e("Inside","Action");
            actionPlaying.playPauseBtnClicked();
        }
    }

    void nextBtnClicked(){
        if (actionPlaying != null)
        {
            Log.e("Inside","Action");
            actionPlaying.nextBtnClicked();
        }
    }

    void prevBtnClicked(){
        if (actionPlaying != null)
        {
            Log.e("Inside","Action");
            actionPlaying.prevBtnClicked();
        }
    }

}

