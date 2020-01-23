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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.view.RowView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.ArrayList;

public class Artist implements SearchResult, Serializable {

    private String name;
    private String url;
    private long id;
    private String image;
    private String biography;
    private String location;

    private ArrayList<Discograph> discography = new ArrayList<>();
    private ArrayList<Show> shows = new ArrayList<>();
    private ArrayList<Site> sites = new ArrayList<>();

    public Artist(JSONObject json) throws JSONException {

        // Parse basic information

        location = json.getString("location");
        try {
            image = Bandcamp.getArtistImageUrl(json.getLong("bio_image_id"));
        } catch (JSONException e) {
            e.printStackTrace(); // some don't have an image
        }
        biography = json.getString("bio");
        name = json.getString("name");

        location = Album.nullToNull(location);
        image = Album.nullToNull(image);
        biography = Album.nullToNull(biography);
        name = Album.nullToNull(name);

        // Parse shows
        try {
            JSONArray showArray = json.getJSONArray("shows");
            for (int i = 0; i < showArray.length(); i++) {
                JSONObject show = showArray.getJSONObject(i);
                shows.add(new Show(show));
            }
        } catch (JSONException e) {
            e.printStackTrace(); // pffff
        }

        // Parse discography
        try {
            JSONArray discographyArray = json.getJSONArray("discography");
            for (int i = 0; i < discographyArray.length(); i++) {
                JSONObject discograph = discographyArray.getJSONObject(i);
                discography.add(new Discograph(discograph));
            }
        } catch (JSONException e) {
            e.printStackTrace(); // pfff
        }

        // Parse sites
        JSONArray sitesArray = json.getJSONArray("sites");
        for (int i = 0; i < sitesArray.length(); i++) {
            JSONObject site = sitesArray.getJSONObject(i);
            sites.add(new Site(site));
        }

    }

    @Deprecated
    public Artist(String html) {

        Document d = Jsoup.parse(html);

        Element bioContainer = d.getElementById("bio-container");

        Element bandNameLocation = bioContainer.getElementById("band-name-location");

        name = bandNameLocation.getElementsByClass("title").first().text();
        location = bandNameLocation.getElementsByClass("location").first().text();

        /*Element bioText = bioContainer.getElementById("bio-text");
        if (bioText != null) {
            biography = bioText.text();
        }*/

        Element descriptionMeta = bioContainer.getElementsByAttributeValue("itemprop", "description").first();
        if (descriptionMeta != null) {
            biography = descriptionMeta.attr("content");
        }

        //largeImage = bioContainer.getElementsByClass("popupImage").first().attr("href");
        image = bioContainer.getElementsByClass("band-photo").first().attr("src");

        Element ol = d.getElementsByAttributeValue("data-edit-callback", "/music-reorder").first();
        // Every a in this ol tag contains one link
        /*Elements as = ol.getElementsByTag("a");

        for (Element a :
                as) {
            String possiblyRelativeUrl = a.attr("href");
        }*/
    }

    public Artist(String name, long id, String image, String location) {
        this.name = name;
        this.id = id;
        this.image = image;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getUrl() { // TODO make elegant somehow
        return String.valueOf(id);
    }

    public String getImage() {
        return image;
    }

    public String getCover() {
        return getImage();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getSubtitle() {
        return getLocation();
    }

    public String getLocation() {
        return location;
    }

    public int getAction() {
        return RowView.ARTIST_OPEN;
    }

    public String getBiography() {
        return biography;
    }

    public ArrayList<Discograph> getDiscography() {
        return discography;
    }

    public ArrayList<Show> getShows() {
        return shows;
    }

    public ArrayList<Site> getSites() {
        return sites;
    }

    public class Show implements Website {
        private String venue;
        private String location;
        private String date;
        private String uri;
        private long utcDate;

        public Show(JSONObject json) throws JSONException {
            venue = json.getString("venue");
            location = json.getString("loc");
            date = json.getString("date");
            uri = json.getString("uri");
            utcDate = json.getLong("utc_date");
        }

        public String getVenue() {
            return venue;
        }

        public String getLocation() {
            return location;
        }

        public String getDate() {
            return date;
        }

        public Uri getUri() {
            return Uri.parse(uri);
        }

        public long getUtcDate() {
            return utcDate;
        }

        @Override
        public String getTitle() {
            return getVenue() + " in " + getLocation() + " on " + getDate();
        }
    }

    /** Things that may appear in a discography */
    public class Discograph {
        private long artId;
        private String artistName;
        private String title;
        private String releaseDate;
        private long itemId;
        private String itemType;
        private long bandId;
        private String bandName;

        public Discograph(JSONObject jsonObject) throws JSONException {

            try {
                artId = jsonObject.getLong("art_id");
            } catch (JSONException e) {
                e.printStackTrace(); // seems to be null
            }
            artistName = Album.nullToNull(jsonObject.getString("artist_name"));
            title = jsonObject.getString("title");
            releaseDate = jsonObject.getString("release_date");
            itemId = jsonObject.getLong("item_id");
            itemType = jsonObject.getString("item_type");
            bandId = jsonObject.getLong("band_id");
            bandName = jsonObject.getString("band_name");


        }

        public void getUrl(Bandcamp bandcamp, final Handler gotIt, final Handler failure) {
            // Query e. g. https://bandcamp.com/api/mobile/22/tralbum_details?band_id=2862267535&tralbum_id=2063639444&tralbum_type=a
            bandcamp.request(
                    "https://bandcamp.com/api/mobile/22/tralbum_details?band_id=" + bandId
                            + "&tralbum_id=" + itemId + "&tralbum_type=" + itemType.substring(0, 1),
                    new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            try {

                                JSONObject jsonObject = new JSONObject(Bandcamp.getHtmlFromMessage(msg));
                                String url = jsonObject.getString("bandcamp_url");

                                // Pack message
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString("url", url.replace("http://", "https://"));
                                message.setData(bundle);

                                // Send message
                                gotIt.sendMessage(message);


                            } catch (JSONException e) {
                                if (failure != null) {
                                    failure.sendEmptyMessage(0);
                                }
                                e.printStackTrace();
                            }
                            return false;
                        }
                    }),
                    failure
            );
        }

        public String getCover(int size) {
            return Bandcamp.getAlbumImageUrl(artId, size);
        }

        public long getArtId() {
            return artId;
        }

        public String getArtistName() {
            return artistName;
        }

        public String getTitle() {
            return title;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public long getItemId() {
            return itemId;
        }

        public String getItemType() {
            return itemType;
        }

        public long getBandId() {
            return bandId;
        }

        public String getBandName() {
            return bandName;
        }
    }

    public class Site implements Website {

        String url;
        String title;

        public Site(JSONObject json) throws JSONException {
            url = json.getString("url");
            title = json.getString("title");
        }

        @Override
        public Uri getUri() {
            return Uri.parse(url);
        }

        @Override
        public String getTitle() {
            return title;
        }
    }
}
