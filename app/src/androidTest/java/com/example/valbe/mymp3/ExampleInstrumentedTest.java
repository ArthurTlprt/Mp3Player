package com.example.valbe.mymp3;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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

        List<Playlist> p = db.PlaylistDao().getAll();

    }
}
