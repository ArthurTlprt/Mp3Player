package com.example.valbe.mymp3;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.valbe.mymp3", appContext.getPackageName());
    }

    @Test
    public void useAppDatabase() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        AppDatabase db = Room.databaseBuilder(appContext,
                AppDatabase.class, "database-name").build();

        Playlist party = new Playlist();
        party.name = "party";

        db.PlaylistDao().insert(party);
        List<Playlist> p = db.PlaylistDao().getAll();

        assertEquals("party", p.get(0).name);

    }

    @Test
    public void UseRelation() throws Exception{
        Context appContext = InstrumentationRegistry.getTargetContext();

        AppDatabase db = Room.databaseBuilder(appContext,
                AppDatabase.class, "Mp3Player").build();

        Playlist party = new Playlist();
        party.name = "party";
        db.PlaylistDao().insert(party);

        Song hit = new Song(2, "Bob Mauranne", "Indochine");
        db.SongDao().insert(hit);

        PlaylistSong ps = new PlaylistSong();
        ps.playlistId = party.id;
        ps.songId = hit.id;
        db.PlaylistSongDao().add(ps);

        List<Song> songs = db.PlaylistSongDao().getPlaylistSong(party.id);

        assertEquals("Indochine", songs.get(2).getArtist());
    }
}
