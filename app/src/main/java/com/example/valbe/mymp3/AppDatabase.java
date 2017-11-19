package com.example.valbe.mymp3;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by arthur on 11/18/17.
 */

@Database(entities = {Playlist.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlaylistDao PlaylistDao();
}