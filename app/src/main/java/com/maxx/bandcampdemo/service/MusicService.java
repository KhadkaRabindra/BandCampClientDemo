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

package com.maxx.bandcampdemo.service;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.*;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.maxx.bandcampdemo.MediaSession;
import com.maxx.bandcampdemo.MusicPlayingNotification;
import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.State;
import com.maxx.bandcampdemo.model.Track;
import com.maxx.bandcampdemo.view.PlayerView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends IntentService {

    public static final String EXTRA_ALBUM = "album";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_POSITION = "position";

    public static final int ACTION_UNDEFINED = 0;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_PLAY = 2;
    public static final int ACTION_SKIP_TRACK = 3;
    public static final int ACTION_BACK_TRACK = 4;
    public static final int ACTION_END = 5;

    private static final int FOREGROUND_ID = 1;

    private static MediaPlayer mediaPlayer;

    private static ArrayList<Track> queue;
    private Album album;

    private static int positionInQueue;

    private Bitmap coverArt;
    private int notificationColor = -1;

    private MediaSession mediaSession;

    public MusicService() {
        super("MusicService");
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        positionInQueue = intent.getIntExtra(EXTRA_POSITION, 0) - 1;
        // Will be iterated on call of nextTrack()

        setAlbum((Album) intent.getSerializableExtra(EXTRA_ALBUM));

        mediaSession = new MediaSession(MusicService.this);

        mediaSession.manage()
                .setCallback(mediaSessionCallback);

        mediaSession.manageMediaMetadata()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album.getTitle());
        // Will be built during playNextTrack()

        // Play first track
        playNextTrack();

        // Create notification
        createNotification();

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".PLAYBACK_CONTROL"));
    }


    private void setAlbum(Album album) {

        this.album = album;
        queue = new ArrayList<>();
        queue.addAll(album.getTracks());
    }

    /**
     * Insert, as in inserting a disc into a media player. Just here, we're inserting a URI into a MediaPlayer.
     */
    private void insertCurrentTrack() throws IOException {

        if (getCurrentTrack().getStream() == null) {
            createNotification();

            // Fetch new steams
            refreshStreams();
        }

        try {
            mediaPlayer.setDataSource(this, Uri.parse(getCurrentTrack().getStream()));
            mediaPlayer.prepare();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void refreshStreams() throws IOException {
        String body = new Bandcamp(null).requestSynchronously(album.getUrl());
        try {
            album.setStreams(new Album(body));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void play() {
        // Reset playback speed
        resetSpeed();

        // Register to AudioManager
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(
                audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        );

        // Start playing
        mediaPlayer.start();

        // Manage media session
        mediaSession.managePlaybackState()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(),
                        getSpeed());
        mediaSession.buildPlaybackState();
    }

    private void pause() {
        mediaPlayer.pause();

        mediaSession.managePlaybackState()
                .setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(),
                        getSpeed());

        createNotification();
    }

    private void playPreviousTrack() {
        positionInQueue -= 2; // To be iterated again
        playNextTrack();
    }

    private void playNextTrack() {
        positionInQueue++;

        makeMediaPlayer();

        try {
            insertCurrentTrack();
            play();

            PlayerView.broadcast(this);

            mediaSession.manageMediaMetadata()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getCurrentTrack().getTitle());
            mediaSession.buildMediaMetadata();

            downloadCoverArt(getCurrentTrack().getCover());

        } catch (IOException e) {
            // Not connected to internet
            e.printStackTrace();
            stopSelf();
        } catch (IllegalStateException e) {
            // Track could not be played
            e.printStackTrace();
            if (isLastTrack(positionInQueue, queue.size())) {
                stopSelf();
            } else {
                Toast.makeText(this, "A track could not be played, skipping", Toast.LENGTH_SHORT).show();
                playNextTrack();
            }
        }

    }

    private void createNotification() {
        Track currentTrack = getCurrentTrack();
        MusicPlayingNotification notification = new MusicPlayingNotification(currentTrack.getTitle(), album.getArtist(),
                coverArt, mediaSession, this)
                // Add back button
                .addButton(MusicPlayingNotification.BUTTON_TYPE_BACK_TRACK);

        // Order: backtrack, play/pause, skip track


        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                notification.addButton(MusicPlayingNotification.BUTTON_TYPE_PAUSE);
            } else if (mediaPlayer != null) {
                notification.addButton(MusicPlayingNotification.BUTTON_TYPE_PLAY);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace(); // why??
        }

        // If this is not the last track, add skip button
        if (!isLastTrack(positionInQueue, queue.size())) {
            notification.addButton(MusicPlayingNotification.BUTTON_TYPE_SKIP_TRACK);
        }

        // Add end button
        notification.addButton(MusicPlayingNotification.BUTTON_TYPE_END);

        // Tint notification
        if (notificationColor != -1) {
            notification.setTint(notificationColor);
        }

        // Give notification to system to get / stay promoted
        this.startForeground(FOREGROUND_ID, notification.build());
    }

    public static boolean isLastTrack(int positionInQueue, int queueSize) {
        return positionInQueue + 1 == queueSize;
    }

    public static Track getCurrentTrack() {
        if (queue == null) return null;
        else return queue.get(positionInQueue);
    }

    private void makeMediaPlayer() {
        // Replace mediaPlayer …throwaway society
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // If this is not the last track, play next track
                PlayerView.broadcast(MusicService.this);
                MusicService.this.createNotification();
            }
        });
    }

    private void downloadCoverArt(String cover) {
        Volley.newRequestQueue(this).add(Bandcamp.makeCoverArtRequest(
                cover,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        coverArt = response;

                        // Make color palette
                        Palette palette = Palette.from(response).generate();

                        notificationColor = palette.getDominantColor(Color.BLACK);


                        // Put bitmap into media session metadata
                        mediaSession.manageMediaMetadata()
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, response);
                        mediaSession.buildMediaMetadata();

                        createNotification();
                    }
                }, null
        ));
    }

    public static State getState() {
        if (mediaPlayer == null) return State.STOPPED;
        if (mediaPlayer.isPlaying()) return State.PLAYING; else return State.PAUSED;
    }


    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver has not been registered…?
            e.printStackTrace();
        }

        MusicPlayingNotification.cancel(MusicService.this);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(audioFocusChangeListener);

        mediaSession.release();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = null;
        positionInQueue = -1;
        queue = null;

        PlayerView.broadcast(this);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handle all kinds of input events

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            if (i == AudioManager.AUDIOFOCUS_LOSS) {
                MusicService.this.pause();
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // BROADCAST RECEIVER: DON'T PUT ONSTARTCOMMAND CODE HERE!

            int action = intent.getIntExtra(EXTRA_ACTION, ACTION_UNDEFINED);

            switch (action) {
                case ACTION_PAUSE:
                    pause();
                    break;

                case ACTION_PLAY:

                    try {
                        play();
                    } catch (IllegalStateException e) {
                        // End of album reached?
                        positionInQueue = -1;
                        playNextTrack();
                    }
                    break;

                case ACTION_SKIP_TRACK:
                    if (positionInQueue < queue.size() - 1) {
                        playNextTrack();
                    }
                    break;

                case ACTION_BACK_TRACK:
                    // If this is not the first track
                    if (positionInQueue != 0) {
                        playPreviousTrack();
                    } else {
                        // Play same track again
                        positionInQueue -= 1;
                        playNextTrack();
                    }
                    break;

                case ACTION_END:
                    stopSelf();

                    // Don't create notification
                    return;
            }

            // Replace old notification
            createNotification();
        }
    };

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {

            KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    play();
                    return true;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    pause();
                    return true;

                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (mediaPlayer.isPlaying()) {
                        pause();
                    } else {
                        play();
                    }
                    return true;

                case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_STEP_FORWARD:
                    // If this is not the last track
                    if (positionInQueue + 1 != queue.size()) {
                        playNextTrack();
                    } else {
                        mediaPlayer.stop();
                    }
                    return true;

                case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                case KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD:
                    // If this is not the first track
                    if (positionInQueue != 0) {
                        playPreviousTrack();
                    } else {
                        // Play same track again
                        positionInQueue -= 1;
                        playNextTrack();
                    }
                    return true;

                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    // Increase playback speed
                    amplifySpeed(1.5f);
                    return true;

                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    // Decrease playback speed
                    amplifySpeed(0.66f);
            }

            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    };

    private void amplifySpeed(float amp) {
        float newPlaybackSpeed = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            newPlaybackSpeed = mediaPlayer.getPlaybackParams().getSpeed() * amp;
            if (newPlaybackSpeed < 4f && newPlaybackSpeed > 0.2f) {
                mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(newPlaybackSpeed));
            }
        } else {
            // Not supported
            Log.d("MUSICSERVICE", "Modifying playback speed is not supported");
        }
    }

    private void resetSpeed() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1f));
        }
    }

    private float getSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mediaPlayer.getPlaybackParams().getSpeed();
        } else {
            return 1;
        }
    }
}
