package com.example.valbe.mymp3;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by arthur on 11/18/17.
 */


@Dao
public interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    List<Playlist> getAll();


}
