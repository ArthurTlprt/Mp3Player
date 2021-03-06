package com.example.valbe.mymp3;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle=false;
    private Random rand;
    public boolean isPrepared = false;
    public boolean isPaused = false;
    public int duration;
    public int musicPosition;

    public MusicController tempController;

    public MusicService() {
    }

    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        rand = new Random();
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public void setSong (int songIndex){
        songPosn = songIndex;
    }

    public void setTempController(MusicController tempController){
        this.tempController = tempController;
    }

    public void playSong() throws IOException {
        isPrepared = false;
        isPaused = false;
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            player.setDataSource(getApplicationContext(),trackUri);
        }
        catch (Exception e){
            Log.e("Music Service", "Error setting data source", e);
        }
        //player.prepare();

        player.prepareAsync();
        Log.e("Music Service", "Musique has been load");
    }

    public int getPosn(){
        musicPosition = player.getCurrentPosition();
        return player.getCurrentPosition();
    }

    public int getDur(){
        duration = player.getDuration();
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        isPaused = true;
        player.pause();
        Log.e("Player Paused", "Player is paused");
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        Log.e("Player play", "Player is playing");
        isPaused = false;
        player.start();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn <= 0){
            songPosn=songs.size()-1;
        }
        try {
            playSong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong >= songPosn){
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        }else{
            songPosn++;
            if(songPosn == songs.size()){
                songPosn=0;
            }
        }
        try {
            playSong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setShuffle(){
        if(shuffle){
            shuffle = false;
        }else{
            shuffle = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    // Release ressources when Service Instance is unbound
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        this.go();
        isPrepared = true;
        Log.e("Player play", "Player is playing on prepared");
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
        tempController.show(0);

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }
}
