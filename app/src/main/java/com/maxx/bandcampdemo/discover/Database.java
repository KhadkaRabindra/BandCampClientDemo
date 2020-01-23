/*
 * campfire, formerly known as bandcampDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Not affiliated with bandcamp, Incorporated.
 */

package com.maxx.bandcampdemo.discover;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.Track;

import java.util.List;

@androidx.room.Database(entities = {Track.class, Album.class}, version = 6, exportSchema = false)
public abstract class Database extends RoomDatabase {

    public static final String DATABASE_NAME = "discover";

    public abstract TrackDao trackDao();
    public abstract AlbumDao albumDao();

    public static Database build(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                Database.class, Database.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public @Nullable Track containsTrack(long id) {
        List<Track> trackList = trackDao().getTrackById(id);
        if (trackList.size() > 0) {
            return trackList.get(0);
        } else {
            return null;
        }
    }

    public @Nullable Album containsAlbum(long id) {
        List<Album> albumList = albumDao().getAlbumById(id);
        if (albumList.size() > 0) {
            return albumList.get(0);
        } else {
            return null;
        }
    }

    public void writeTrack(Track track) {

        if (containsTrack(track.getId()) == null) {
            trackDao().insert(track);
        } else {
            trackDao().update(track);
        }
    }

    public void writeAlbum(Album album) {

        if (containsAlbum(album.getId()) == null) {
            albumDao().insert(album);
        } else {
            albumDao().update(album);
        }
    }

    public void writeAlbumTracksAsync(final Album album) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Track t :
                        album.getTracks()) {
                    Database.this.writeTrack(t);
                }
                Database.this.writeAlbum(album);
            }
        }).start();
    }

    public void flush() {
        trackDao().drop();
        albumDao().drop();
    }

    public void drop(Album a) {
        for (Track t :
                a.getTracks()) {
            drop(t);
        }
        albumDao().drop(a.getId());
    }

    public void drop(Track t) {
        trackDao().drop(t.getId());
    }
}
