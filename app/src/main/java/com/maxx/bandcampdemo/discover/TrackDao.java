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

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.maxx.bandcampdemo.model.Track;

import java.util.List;

@Dao
public interface TrackDao {
    @Query("SELECT * FROM Track")
    List<Track> getAll();

    @Query("SELECT * FROM Track WHERE id IS :id")
    List<Track> getTrackById(long id);

    @Query("SELECT * FROM Track WHERE album is :id")
    List<Track> getTracksByAlbumId(long id);

    @Insert
    void insert(Track track);

    @Update
    void update(Track track);

    @Query("SELECT COUNT() FROM Track")
    int count();

    @Query("DELETE FROM Track")
    void drop();

    @Query("DELETE FROM Track WHERE :id IS id")
    void drop(long id);
}
