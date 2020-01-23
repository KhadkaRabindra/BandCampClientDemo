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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.maxx.bandcampdemo.SharedPreferences;
import com.maxx.bandcampdemo.ZoomImage;
import com.maxx.bandcampdemo.adapters.TrackAdapter;
import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.base.BaseActivity;
import com.maxx.bandcampdemo.discover.Database;
import com.maxx.bandcampdemo.inter.TrackListener;
import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.Track;
import com.maxx.bandcampdemo.model.User;
import com.maxx.bandcampdemo.service.MusicService;
import com.maxx.bandcampdemo.view.PlayerView;

import org.json.JSONException;

import godau.fynn.bandcampdirect.R;


public class AlbumActivity extends BaseActivity {

    RecyclerView recyclerView;

    public static final String EXTRA_TRACK = "track";
    public static final String EXTRA_ALBUM = "album";

    private ZoomImage zoomImage = new ZoomImage();

    private Database database;
    private Uri uri;
    private User user;

    PlayerView playerView;
    TextView paid;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_album;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paid = findViewById(R.id.paid);
        recyclerView = (RecyclerView) findViewById(R.id.albumRecyclerView);

        uri = getIntent().getData();
        SharedPreferences sharedPreferences = new SharedPreferences(AlbumActivity.this);
        final TextView paid = findViewById(R.id.paid);

        user = new User(sharedPreferences.getString(SharedPreferences.IDENTITY_TOKEN, null));

        // Database
        database = Database.build(AlbumActivity.this);

        if (getIntent().hasExtra(EXTRA_TRACK)) {
            // The track has already been downloaded, don't download it again

            Album album = (Album) getIntent().getSerializableExtra(EXTRA_ALBUM);
            int trackPosition = getIntent().getIntExtra(EXTRA_TRACK, 0);
            Track track = album.getTrack(trackPosition);

            // Display track of album
            displayTrack(track, album);

            // Mr. Red, come out to play
            playerView = new PlayerView(AlbumActivity.this, album, trackPosition);

            // Link artist
            linkArtist(album);

            // Display cover art
            displayCoverArt(track.getCover());
        } else if (getIntent().hasExtra(EXTRA_ALBUM)) {
            // The album should be viewed

            Album album = (Album) getIntent().getSerializableExtra(EXTRA_ALBUM);
            displayAlbum(album, true);

            // Plaaaaay…
            playerView = new PlayerView(AlbumActivity.this, album);

            // Link artist
            linkArtist(album);

            // Display cover art
            displayCoverArt(album.getCover());
        } else {

            downloadTrack();
        }
    }

    private void downloadTrack() {

        // Download track
        final Bandcamp bandcamp = new Bandcamp(user);
        Handler success = new Handler(message -> {
            try {
                String html = Bandcamp.getHtmlFromMessage(message);

                // Parse html
                final Album album = new Album(html);

                // Title to user name
                if (user.getDisplayName() != null) {
                    AlbumActivity.this.setTitle(user.getDisplayName() + " | " + AlbumActivity.this.getString(R.string.app_name));
                }

                // Display album or individual track
                if (album.getTracks().size() > 1) {
                    AlbumActivity.this.displayAlbum(album, false);

                    discoverTracks(album, bandcamp, database, new Handler(
                            new Handler.Callback() {
                                @Override
                                public boolean handleMessage(@NonNull Message message1) {
                                    // All tracks were discovered, album can be displayed
                                    AlbumActivity.this.displayAlbum(album, true);
                                    return false;
                                }
                            }
                    ), new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            paid.setText("Could not discover");

                            return false;
                        }
                    }));
                } else {
                    AlbumActivity.this.displayTrack(album.getTrack(0), album);

                    // Save to discover database
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            database.writeTrack(album.getTrack(0));
                            database.writeAlbum(album);
                        }
                    }).start();

                }

                // Player
                //preparePlayer(album);

                new PlayerView(AlbumActivity.this, album);

                // Link artist
                AlbumActivity.this.linkArtist(album);

                AlbumActivity.this.displayCoverArt(album.getCover());

            } catch (JSONException | IndexOutOfBoundsException e) {
                AlbumActivity.this.showInfo("No information can be displayed…");
                e.printStackTrace();
            }

            return false;
        });


        String url;
        if (uri != null) {
            url = uri.toString();
        } else {
            url = "https://zachbenson.bandcamp.com/album/prom";
        }

        bandcamp.request(url, success, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                AlbumActivity.this.showInfo("A network error occurred");
                return false;
            }
        }));
    }

    private void linkArtist(final Album album) {
        // Link to artist
        findViewById(R.id.artist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumActivity.this, ArtistActivity.class);
                intent.putExtra(ArtistActivity.ARTIST_ID, album.getArtistId());
                AlbumActivity.this.startActivity(intent);
            }
        });
    }

    public void discoverTracks(final Album album, Bandcamp bandcamp, final Database database, final Handler then, Handler fail) {

        final int[] discoveredTracks = {0};

        for (final Track track : album.getTracks()) {
            String host = Uri.parse(album.getUrl()).getHost();
            String url = "https://" + host + track.getPath();

            bandcamp.request(url, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message message) {

                    String html = Bandcamp.getHtmlFromMessage(message);

                    try {
                        Album discoveredAlbum = new Album(html);
                        Track discoveredTrack = discoveredAlbum.getTrack(0);

                        track.update(discoveredTrack);


                        discoveredTracks[0]++;
                        // If this was the last track
                        if (discoveredTracks[0] >= album.getTracks().size()) {
                            // Done
                            if (then != null) {
                                then.sendEmptyMessage(0);
                            }

                            // Save to discover database
                            database.writeAlbumTracksAsync(album);
                        }

                    } catch (JSONException | ArrayIndexOutOfBoundsException e) {
                        dismissProgressDialog();
                        e.printStackTrace();
                        discoveredTracks[0]++;
                        // If this was the last track
                        if (discoveredTracks[0] >= album.getTracks().size()) {
                            // Done
                            if (then != null) {
                                then.sendEmptyMessage(0);
                            }

                            // Save to discover database
                            database.writeAlbumTracksAsync(album);
                        }
                    }

                    return false;
                }
            }), fail);
        }
    }

    private void displayTrack(Track track, Album album) {

        // Show metadata
        displayMeta(track.getTitle(), album.getArtist());


        // Lyrics
        showLyrics(track.getLyrics());
        // if hasLyrics is set but getLyrics returned null, lyrics were not provided


        // Info
        showInfo(track.getInfo());


        // Paid text view

        if (track.isPaid()) {
            paid.setText("♥");
        } else if (album.isStreamingLimitEnabled()) {
            paid.setText("Streaming limit: " + album.getStreamingLimit());
        } else {
            paid.setText("Unpaid");
        }
    }

    private void displayAlbum(Album album, boolean discovered) {

        // Display metadata
        displayMeta(album.getTitle(), album.getArtist());

        boolean allPaid = true;
        boolean somePaid = false;

        showInfo(album.getAbout());

        TrackAdapter adapter = new TrackAdapter(getApplicationContext(), album.getTracks(), album, new TrackListener() {
            @Override
            public void onItemClick(Track searchResult, int posiiton) {
                play(getApplicationContext(), album, posiiton);

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        /*LinearLayout tracksLayout = (LinearLayout) findViewById(R.id.trackLayout);

        for (int i = 0; i < album.getTracks().size(); i++) {
            // Create view for track
            new TrackRowView(this, tracksLayout, album, i, discovered);

            // Determine whether album was purchased
            if (!album.getTrack(i).isPaid()) {
                allPaid = false;
            } else {
                somePaid = true;
            }
        }*/


        // Display paid text
        TextView paid = findViewById(R.id.paid);

        if (discovered) {
            if (album.isPaid()) {
                paid.setText("♥");
            } else if (allPaid) {
                paid.setText("[♥]");
            } else if (somePaid) {
                paid.setText("Some tracks unpaid; streaming limit: " + album.getStreamingLimit());
            } else if (album.isStreamingLimitEnabled()) {
                paid.setText("Streaming limit: " + album.getStreamingLimit());
            } else {
                paid.setText("Unpaid");
            }
        } else {
            paid.setText("Discovering");
        }
    }

    private void showLyrics(@Nullable String lyrics) {
        /*if (lyrics != null) {
            findViewById(R.id.lyricsLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.lyrics)).setText(lyrics);

            // Hide headline if first word is "lyrics"
            if (lyrics.toLowerCase().startsWith("lyrics")) {
                findViewById(R.id.lyricsTitle).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.lyricsLayout).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.GONE);
        }*/
    }

    void showInfo(@Nullable String info) {
        /*if (info != null) {
            findViewById(R.id.infoLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.info)).setText(info);
        } else {
            findViewById(R.id.infoLayout).setVisibility(View.GONE);
        }*/
    }

    void displayMeta(String title, String artist) {
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

    public static void play(Context context, Album album, int which) {
        Intent intent = new Intent(context, MusicService.class);

        intent.putExtra(MusicService.EXTRA_ALBUM, album);
        intent.putExtra(MusicService.EXTRA_POSITION, which);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

    }

    void displayCoverArt(String cover) {
        // Cover art

        final ImageView coverView = findViewById(R.id.cover);

        Volley.newRequestQueue(AlbumActivity.this).add(Bandcamp.makeCoverArtRequest(
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            recreate();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (playerView != null) {
            playerView.onDestroy();
        }

        super.onDestroy();
    }


}
