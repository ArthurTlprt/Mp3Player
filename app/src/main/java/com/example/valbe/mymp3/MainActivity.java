package com.example.valbe.mymp3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;


public class MainActivity extends AppCompatActivity implements MediaPlayerControl, NavigationView.OnNavigationItemSelectedListener{

    //For showing existing musics
    private ArrayList<Song> songList;
    private ListView songView;

    //For music service
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    public MusicController controller;

    private boolean paused = false;
    private boolean playbackPause = false;

    private boolean ischecked = true;

    SongAdapter songAdt;



    // In te on create, we create the view and we are querying musics from internal database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        if(ischecked){
            Collections.sort(songList, new Comparator<Song>() {
                @Override
                public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
                }
            });
            songAdt = new SongAdapter(this, songList);
            songView.setAdapter(songAdt);
            setController();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                controller.hide();
                Log.e("Final search","It's me on final");
                songAdt.getFilter().filter(query);
                songView.setAdapter(songAdt);
                musicSrv.setList(songAdt.getSongs());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                controller.hide();
                Log.e("Final search","No it's me");
                songAdt.getFilter().filter(newText);
                songView.setAdapter(songAdt);
                musicSrv.setList(songAdt.getSongs());
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                songAdt.setInitSongList(songList);
                songView.setAdapter(songAdt);
                musicSrv.setList(songAdt.getSongs());
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.search:
                controller.hide();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop(){
        controller.hide();
        super.onStop();
    }

    // To stop the music if app is destroyed
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }


    // Method to query songList, wich ask user permission if this is the first time
    public void getSongList(){
        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {

            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
            if(musicCursor!=null && musicCursor.moveToFirst()){
                //get columns
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                //add songs to list
                Playlist p = new Playlist();
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    Log.v("id", ""+thisId);
                    Song song = new Song(thisId, thisTitle, thisArtist);
                    //p.songs.add(song);
                    songList.add(new Song(thisId, thisTitle, thisArtist));
                }
                while (musicCursor.moveToNext());
                Log.v("playlist", ""+p.uid);
            }
        }else{
            ischecked = false;
        }
    }


    // Serial of code to check uses permission to acceed music, indispensible
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                    return false;
                } else {
                    ActivityCompat.requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getSongList();

                    Collections.sort(songList, new Comparator<Song>() {
                        @Override
                        public int compare(Song o1, Song o2) {
                            return o1.getTitle().compareTo(o2.getTitle());
                        }
                    });

                    songAdt = new SongAdapter(this, songList);
                    songView.setAdapter(songAdt);

                    setController();

                } else Toast.makeText(this, "GET_ACCOUNTS Denied",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    //Equivalent of onClick for elements of the list
    public void songPicked (View view){
        Log.e("Test Search", view.getTag().toString());
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        try {
            musicSrv.setTempController(controller);
            musicSrv.playSong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("Music Service Main", "Je suis la");
        if(playbackPause){
            //setController();
            playbackPause = false;
        }

        //controller.show(0);

    }

    /*  All those methodes are for control of the music */
    @Override
    public void start() {
        playbackPause = false;
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPause = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && isPlaying()){
            Log.e("TEST MUSIQUE", "GET DURATION");

            return musicSrv.getDur();
        }else{
            Log.e("TEST MUSIQUE", "NOT GET DURATION");
            if(musicSrv.isPaused){
                return musicSrv.duration;
            }
            return 0;
        }

    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()){
            return musicSrv.getPosn();
        }else{
            if(musicSrv.isPaused == true){
                return musicSrv.musicPosition;
            }
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.musicPosition = pos;
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound){
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        controller = null;
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.drawer_layout));
        //controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void playNext(){
        musicSrv.setTempController(controller);
        musicSrv.playNext();
        if(playbackPause){
            //setController();
            playbackPause=false;
        }
        //controller.show(0);
    }

    private void playPrev(){
        musicSrv.setTempController(controller);
        musicSrv.playPrev();
        if(playbackPause){
            //setController();
            playbackPause=false;
        }
        //controller.show(0);
    }
}
