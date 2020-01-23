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

package com.maxx.bandcampdemo.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.maxx.bandcampdemo.activity.AlbumActivity;
import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.State;
import com.maxx.bandcampdemo.model.Track;
import com.maxx.bandcampdemo.service.MusicService;

import godau.fynn.bandcampdirect.R;

import static com.maxx.bandcampdemo.service.MusicService.ACTION_BACK_TRACK;
import static com.maxx.bandcampdemo.service.MusicService.ACTION_END;
import static com.maxx.bandcampdemo.service.MusicService.ACTION_PAUSE;
import static com.maxx.bandcampdemo.service.MusicService.ACTION_PLAY;
import static com.maxx.bandcampdemo.service.MusicService.ACTION_SKIP_TRACK;

public class PlayerView {

    private Activity context;

    private Album album;

    private int trackPosition;

    private ImageButton playButton;
    private ImageButton skipButton;
    private ImageButton backtrackButton;
    private ImageButton endButton;
    private TextView trackText;

    public PlayerView(Activity context, Album album) {
        this(context, album, 0);
    }

    public PlayerView(Activity context, Album album, int trackPosition) {
        this.context = context;

        this.album = album;

        this.trackPosition = trackPosition;

        init();
    }

    private void init() {

        context.registerReceiver(broadcastReceiver, new IntentFilter(context.getPackageName() + ".PLAYBACK_CONTROL"));

        playButton = context.findViewById(R.id.play);
        skipButton = context.findViewById(R.id.skip);
        backtrackButton = context.findViewById(R.id.backtrack);
        endButton = context.findViewById(R.id.end);
        trackText = context.findViewById(R.id.trackName);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (MusicService.getState()) {

                    case STOPPED:
                        AlbumActivity.play(context, album, trackPosition);
                        trackText.setText(album.getTrack(trackPosition).getTitle());
                        break;

                    case PLAYING:
                        PlayerView.this.broadcast(ACTION_PAUSE);
                        break;

                    case PAUSED:
                        PlayerView.this.broadcast(ACTION_PLAY);
                        break;

                }

            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerView.this.broadcast(ACTION_SKIP_TRACK);
            }
        });
        backtrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerView.this.broadcast(ACTION_BACK_TRACK);
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerView.this.broadcast(ACTION_END);
            }
        });

        updateDisplay();

    }

    private void updateDisplay() {

        Track playing = MusicService.getCurrentTrack();
        if (playing != null) {
            trackText.setText(playing.getTitle());
        }

        State s = MusicService.getState();
        switch (s) {

            case STOPPED:
                trackText.setText("");
            case PAUSED:
                playButton.setImageDrawable(context.getDrawable(R.drawable.ic_play));
                break;
            case PLAYING:
                playButton.setImageDrawable(context.getDrawable(R.drawable.ic_pause));
                break;
        }

        int visibility = s == State.STOPPED ? View.INVISIBLE : View.VISIBLE;
        endButton.setVisibility(visibility);
        backtrackButton.setVisibility(visibility);
        skipButton.setVisibility(visibility);
    }

    public void broadcast(int action) {
        Intent intent = new Intent(context.getPackageName() + ".PLAYBACK_CONTROL");
        intent.putExtra(MusicService.EXTRA_ACTION, action);

        context.sendBroadcast(intent);
    }

    public static void broadcast(Context context) {
        Intent intent = new Intent(context.getPackageName() + ".PLAYBACK_CONTROL");

        context.sendBroadcast(intent);
    }


    public void onDestroy() {
        try {
            context.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver has not been registered…?
            e.printStackTrace();
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDisplay();
        }
    };
}
