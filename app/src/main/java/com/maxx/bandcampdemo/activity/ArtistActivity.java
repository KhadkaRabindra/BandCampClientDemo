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

package com.maxx.bandcampdemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.maxx.bandcampdemo.SharedPreferences;
import com.maxx.bandcampdemo.ZoomImage;
import com.maxx.bandcampdemo.adapters.ArtistAdapter;
import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.base.BaseActivity;
import com.maxx.bandcampdemo.inter.ArtistClickListener;
import com.maxx.bandcampdemo.model.Artist;
import com.maxx.bandcampdemo.view.WebsiteRowView;

import godau.fynn.bandcampdirect.R;


public class ArtistActivity extends BaseActivity {
    RecyclerView recyclerView;

    public static final String EXTRA_ARTIST = "artist";
    public static final String ARTIST_ID = "id";

    private ZoomImage zoomImage = new ZoomImage();
    private Bandcamp bandcamp;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_artist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = (RecyclerView) findViewById(R.id.searchRecyclerView);

        SharedPreferences sharedPreferences = new SharedPreferences(ArtistActivity.this);
        bandcamp = new Bandcamp(sharedPreferences.createUser());


        if (getIntent().hasExtra(EXTRA_ARTIST)) {

            // View artist

            Artist artist = (Artist) getIntent().getSerializableExtra(EXTRA_ARTIST);

            displayArtist(artist);

            displayCoverArt(artist.getImage());
        } else {

            // Download artist from ID
            bandcamp.postArtistDetails(getIntent().getLongExtra(ARTIST_ID, -1), new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {

                    Artist artist = (Artist) msg.getData().getSerializable(Bandcamp.ARTIST);

                    ArtistActivity.this.displayArtist(artist);

                    ArtistActivity.this.displayCoverArt(artist.getImage());

                    return false;
                }
            }), new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    ArtistActivity.this.showInfo("A network error occurred");
                    return false;
                }
            }));

        }
    }

    private void displayArtist(Artist artist) {

        displayMeta(artist.getName(), artist.getLocation());

        showInfo(artist.getBiography());

        if (artist.getDiscography().size() > 0) {

            /*LinearLayout tracksLayout = findViewById(R.id.tracks);
            tracksLayout.setVisibility(View.VISIBLE);*/


            /*for (Artist.Discograph discograph :
                    artist.getDiscography()) {

                RowView rowView = new DiscographRowView(ArtistActivity.this, tracksLayout, discograph, bandcamp);

                MainActivity.downloadCover(ArtistActivity.this, rowView, discograph.getCover(3));

            }*/

            ArtistAdapter adapter = new ArtistAdapter(getApplicationContext(), artist.getDiscography(), (ArtistClickListener) discograph -> {
                discograph.getUrl(bandcamp, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        String url = msg.getData().getString("url");

                        Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);

                        return false;
                    }
                }), null);

            });
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);
        }

        if (artist.getShows().size() > 0) {

            LinearLayout showLayout = findViewById(R.id.shows);
            findViewById(R.id.showsLayout).setVisibility(View.VISIBLE);

            for (Artist.Show show : artist.getShows()) {
                new WebsiteRowView(ArtistActivity.this, showLayout, show);
            }
        }
    }

    private void displayMeta(String title, String artist) {
        // Title text view
        TextView titleView = findViewById(R.id.title);
        titleView.setText(title);

        // Artist text view
        if (artist != null) {
            ((TextView) findViewById(R.id.artist)).setText(artist);
        }

        // Show divider
        findViewById(R.id.divider).setVisibility(View.VISIBLE);
    }

    private void showInfo(@Nullable String info) {
        /*if (info != null) {
            findViewById(R.id.infoLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.info)).setText(info);
        } else {
            findViewById(R.id.infoLayout).setVisibility(View.GONE);
        }*/
    }

    private void displayCoverArt(String cover) {
        // Cover art

        final ImageView coverView = findViewById(R.id.cover);

        Volley.newRequestQueue(ArtistActivity.this).add(Bandcamp.makeCoverArtRequest(
                cover, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        final BitmapDrawable bitmapDrawable = new BitmapDrawable(response);
                        coverView.setImageDrawable(bitmapDrawable);

                        coverView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                zoomImage.zoomImage(coverView, bitmapDrawable,
                                        findViewById(R.id.expandedCover), findViewById(R.id.root));
                            }
                        });
                    }
                }, null));
    }
}
