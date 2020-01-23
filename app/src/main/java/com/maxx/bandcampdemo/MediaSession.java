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

package com.maxx.bandcampdemo;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaSession {

    private MediaSessionCompat mediaSession;

    private PlaybackStateCompat.Builder playbackStateBuilder;
    private MediaMetadataCompat.Builder mediaMetadataBuilder;

    public MediaSession(Context context) {
        mediaSession = new MediaSessionCompat(context, "albumPlayback");

        mediaSession.setActive(true);

        playbackStateBuilder = new PlaybackStateCompat.Builder();
        mediaMetadataBuilder = new MediaMetadataCompat.Builder();
    }

    public PlaybackStateCompat.Builder managePlaybackState() {
        return playbackStateBuilder;
    }

    public MediaMetadataCompat.Builder manageMediaMetadata() {
        return mediaMetadataBuilder;
    }

    public MediaSessionCompat manage() {
        return mediaSession;
    }

    public void buildPlaybackState() {
        mediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    public void buildMediaMetadata() {
        mediaSession.setMetadata(mediaMetadataBuilder.build());
    }

    public void release() {
        mediaSession.release();
    }

}
