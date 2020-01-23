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

import com.maxx.bandcampdemo.model.Album;


import java.util.List;

@Dao
public interface AlbumDao {
    @Query("SELECT * FROM Album ORDER BY artist")
    List<Album> getAll();

    @Query("SELECT * FROM Album WHERE id IS :id")
    List<Album> getAlbumById(long id);

    @Insert
    void insert(Album album);

    @Update
    void update(Album album);

    @Query("SELECT COUNT() FROM Album")
    int count();

    @Query("DELETE FROM Album")
    void drop();

    @Query("DELETE FROM Album WHERE id IS :id")
    void drop(long id);
}
