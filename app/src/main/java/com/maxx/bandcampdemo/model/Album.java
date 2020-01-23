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

import android.net.Uri;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.view.RowView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@Entity(tableName = "Album")
public class Album implements SearchResult, Serializable {

    @Ignore
    private ArrayList<Track> tracks = new ArrayList<>();

    @ColumnInfo(name = "streamingLimit")
    private int streamingLimit;
    @ColumnInfo(name = "streamingLimitEnabled")
    private boolean streamingLimitEnabled;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "about")
    @Nullable
    private String about;

    @ColumnInfo(name = "credits")
    @Nullable
    private String credits;

    @ColumnInfo(name = "paid")
    private boolean paid;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "artistId")
    private long artistId;

    @PrimaryKey
    private long id;
    private String cover;

    public Album(String html) throws JSONException {
        JSONObject albumJson = getAlbumInfoJson(html);

        // Parse whether this album was purchased
        paid = Bandcamp.getBooleanNullsave(albumJson, "is_purchased");

        // Parse streaming limits
        try {
            JSONObject streamingLimits = albumJson.getJSONObject("play_cap_data");
            streamingLimitEnabled = streamingLimits.getBoolean("streaming_limits_enabled");
            streamingLimit = streamingLimits.getInt("streaming_limit");
        } catch (JSONException e) {
            streamingLimit = 0;
            streamingLimitEnabled = false;
        }

        // Get "current"
        JSONObject current = albumJson.getJSONObject("current");

        // Parse title, about, credits, id, artistId
        title = current.getString("title");
        about = nullToNull(current.getString("about"));
        credits = nullToNull(current.getString("credits"));
        id = current.getLong("id");
        artistId = current.getLong("selling_band_id");

        // Parse artist and url
        artist = albumJson.getString("artist");
        url = albumJson.getString("url");

        // Get cover
        cover = getCoverArtUrl(html);

        // Parse tracks

        JSONArray trackJsons = albumJson.getJSONArray("trackinfo");

        for (int i = 0; i < trackJsons.length(); i++) {
            Track t = new Track(trackJsons.getJSONObject(i));

            // Set album id
            t.setAlbumId(id);

            // Set track number
            t.setTrackNumber(i);

            if (paid) {
                // Inherit paid value from album
                t.setPaid(true);
            }
            if (t.getInfo() != null && t.getInfo().equals("true")) {
                // Inherit info value from album's about value (because bandcamp seems to work that way)
                t.setInfo(about);
            }

            // Inherit cover
            t.setCover(cover);

            tracks.add(t);
        }

    }

    public Album(int streamingLimit, boolean streamingLimitEnabled, String title, String artist, @Nullable String about, @Nullable String credits, boolean paid, String url, long id, String cover, long artistId) {
        this.streamingLimit = streamingLimit;
        this.streamingLimitEnabled = streamingLimitEnabled;
        this.title = title;
        this.artist = artist;
        this.about = about;
        this.credits = credits;
        this.paid = paid;
        this.url = url;
        this.id = id;
        this.cover = cover;
        this.artistId = artistId;
    }

    @Ignore
    public Album(String title, String artist, String url, String cover) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.cover = cover;
    }

    public static String nullToNull(@Nullable String s) {
        if (s != null && s.equals("null")) {
            return null;
        } else {
            return s;
        }
    }

    private static JSONObject getAlbumInfoJson(String html) throws JSONException {
        return Bandcamp.getJSONFromJavaScriptVariables(html, "TralbumData");
    }

    public static String getCoverArtUrl(String html) {
        Document document = Jsoup.parse(html);
        return document.getElementsByAttributeValue("property", "og:image").get(0).attr("content");
        // Remove before
        //String[] split = html.split("<meta property=\"og:image\" content=\"");
        // Remove after
        //String url = split[1].replaceAll("\"(.|\\n)*", "");
    }

    public String getTrackUrl(Track track) {
        String path = track.getPath();
        Uri albumUri = Uri.parse(url);
        String url = albumUri.getScheme() + "://" + albumUri.getHost() + path;
        Log.d("TRACKURI", url);
        return url;
    }

    public void orderTracks() {
        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track track, Track t1) {
                return Integer.compare(track.getTrackNumber(), t1.getTrackNumber());
            }
        });
    }

    public boolean isImplicitlyPaid() {

        if (tracks.size() == 0) {
            return false;
        }

        boolean yes = true;
        for (Track t :
                tracks) {
            if (!t.isPaid()) {
                yes = false;
            }
        }
        return yes;
    }

    public void setStreams(Album album) {
        for (int i = 0; i < tracks.size(); i++) {
            Track track = getTrack(i);
            Track newTrack = album.getTrack(i);
            track.setStream(newTrack.getStream());
        }
    }

    public String getSubtitle() {
        return artist;
    }

    public int getStreamingLimit() {
        return streamingLimit;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public boolean isPaid() {
        return paid;
    }

    public boolean isStreamingLimitEnabled() {
        return streamingLimitEnabled;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAbout() {
        return about;
    }

    public String getCredits() {
        return credits;
    }

    public String getCover() {
        return cover;
    }

    public long getId() {
        return id;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Track getTrack(int index) {
        return tracks.get(index);
    }

    public String getUrl() {
        return url.replace("http://", "https://");
    }

    public void setStreamingLimit(int streamingLimit) {
        this.streamingLimit = streamingLimit;
    }

    public void setStreamingLimitEnabled(boolean streamingLimitEnabled) {
        this.streamingLimitEnabled = streamingLimitEnabled;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAbout(@Nullable String about) {
        this.about = about;
    }

    public void setCredits(@Nullable String credits) {
        this.credits = credits;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public int getAction() {
        return RowView.OPEN;
    }

}
