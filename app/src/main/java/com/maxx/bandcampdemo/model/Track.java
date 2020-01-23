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

package com.maxx.bandcampdemo.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.view.RowView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@Entity(tableName = "Track")
public class Track implements SearchResult, Serializable {

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "paid")
    private boolean paid;

    @ColumnInfo(name = "lyrics")
    private @Nullable
    String lyrics;

    @ColumnInfo(name = "info")
    private @Nullable
    String info;

    private boolean hasLyrics;

    @ColumnInfo(name = "album")
    private long albumId;

    @ColumnInfo(name = "path")
    private String path;

    @ColumnInfo(name = "trackNumber")
    private int trackNumber;

    @PrimaryKey
    private long id;

    @Ignore
    private String stream;

    @ColumnInfo(name = "cover")
    private String cover;

    // Only for search results
    @Ignore
    private String artist;
    @Ignore
    private String albumName;

    /**
     * Make Track from track JSON object
     */
    public Track(JSONObject trackJson) throws JSONException {

        lyrics = trackJson.getString("lyrics");
        if (lyrics.equals("null")) {
            lyrics = null;
        }

        info = trackJson.getString("has_info");
        if (info.equals("null") || info.equals("false")) {
            // If has_info is true, it will be inherited from parent
            info = null;
        }

        hasLyrics = Bandcamp.getBooleanNullsave(trackJson, "has_lyrics");

        id = trackJson.getLong("id");

        title = trackJson.getString("title");

        path = trackJson.getString("title_link");

        try {
            stream = trackJson.getJSONObject("file").getString("mp3-128");
        } catch (JSONException e) {
            // no stream
            e.printStackTrace();
        }
    }

    public Track(String title, boolean paid, @Nullable String lyrics, @Nullable String info, boolean hasLyrics, long albumId, String path, int trackNumber, long id, String cover) {
        this.title = title;
        this.paid = paid;
        this.lyrics = lyrics;
        this.info = info;
        this.hasLyrics = hasLyrics;
        this.albumId = albumId;
        this.path = path;
        this.trackNumber = trackNumber;
        this.id = id;
        this.cover = cover;
    }

    @Ignore
    public Track(String title, String path, String cover, String artist, String albumName) {
        this.title = title;
        this.path = path;
        this.cover = cover;
        this.artist = artist;
        this.albumName = albumName;
    }

    public void update(Track updatedTrack) {

        if (updatedTrack.isPaid()) {
            paid = true;
        }

        String updatedLyrics = updatedTrack.getLyrics();
        if (updatedLyrics != null) {
            lyrics = updatedTrack.getLyrics();
        }

        String updatedInfo = updatedTrack.getInfo();
        if (updatedInfo != null) {
            info = updatedInfo;
        }

        String updatedCover = updatedTrack.getCover();
        if (updatedCover != null) {
            cover = updatedCover;
        }
    }

    public boolean hasExtras() {

        boolean hasNoInfo = info == null
                || info.isEmpty();

        return !hasNoInfo || hasLyrics;
    }

    public String getSubtitle() {
        return albumName + " | " + artist;
    }

    public String getUrl() {
        return path;
    }

    // GETTER / SETTER ZONE

    public void setHasLyrics(boolean hasLyrics) {
        this.hasLyrics = hasLyrics;
    }

    public String getTitle() {
        return title;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getStream() {
        return stream;
    }

    public boolean hasLyrics() {
        return hasLyrics;
    }

    @Nullable
    public String getInfo() {
        return info;
    }

    public boolean isPaid() {
        return paid;
    }

    public long getId() {
        return id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void setLyrics(@Nullable String lyrics) {
        this.lyrics = lyrics;
    }

    public void setInfo(@Nullable String info) {
        this.info = info;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public int getAction() {
    return RowView.OPEN;
    }
}
