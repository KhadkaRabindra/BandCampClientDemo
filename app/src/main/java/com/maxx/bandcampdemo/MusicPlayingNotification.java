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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.maxx.bandcampdemo.service.MusicService;

import godau.fynn.bandcampdirect.R;

public class MusicPlayingNotification {

    private NotificationCompat.Builder notificationBuilder;

    private Context context;

    private static final String CHANNEL = "musicPlaying";
    private static final int ID = 0;

    private static final int REQUEST_CODE_PAUSE = 0;
    private static final int REQUEST_CODE_PLAY = 1;
    private static final int REQUEST_CODE_SKIP = 2;
    private static final int REQUEST_CODE_BACK = 3;
    private static final int REQUEST_CODE_END = 4;

    public static final int BUTTON_TYPE_PAUSE = 1;
    public static final int BUTTON_TYPE_PLAY = 2;
    public static final int BUTTON_TYPE_SKIP_TRACK = 3;
    public static final int BUTTON_TYPE_BACK_TRACK = 4;
    public static final int BUTTON_TYPE_END = 5;

    public MusicPlayingNotification(String title, String artist, Bitmap coverArt, MediaSession mediaSession, Context context) {

        this.context = context;

        createChannel();

        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL)
                // This notification should always be visible on lock screen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Set media style
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.manage().getSessionToken())
                        .setShowCancelButton(true)
                )
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.notification)
                .setShowWhen(false)
                .setOngoing(true);

        if (coverArt != null) {
            notificationBuilder.setLargeIcon(coverArt);
        }
    }

    public MusicPlayingNotification addButton(int type) {

        Intent intent = new Intent(context.getPackageName() + ".PLAYBACK_CONTROL");

        switch (type) {
            case BUTTON_TYPE_PAUSE:


                intent.putExtra(MusicService.EXTRA_ACTION, MusicService.ACTION_PAUSE);

                notificationBuilder.addAction(
                        R.drawable.ic_pause, "PAUSE!!", PendingIntent.getBroadcast(context, REQUEST_CODE_PAUSE,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );
                return this;
            case BUTTON_TYPE_PLAY:

                intent.putExtra(MusicService.EXTRA_ACTION, MusicService.ACTION_PLAY);

                notificationBuilder.addAction(
                        R.drawable.ic_play, "PLAY!!", PendingIntent.getBroadcast(context, REQUEST_CODE_PLAY,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );

                return this;

            case BUTTON_TYPE_SKIP_TRACK:

                intent.putExtra(MusicService.EXTRA_ACTION, MusicService.ACTION_SKIP_TRACK);

                notificationBuilder.addAction(
                        R.drawable.ic_next, "NEXT!!", PendingIntent.getBroadcast(context, REQUEST_CODE_SKIP,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );

                return this;

            case BUTTON_TYPE_BACK_TRACK:

                intent.putExtra(MusicService.EXTRA_ACTION, MusicService.ACTION_BACK_TRACK);

                notificationBuilder.addAction(
                        R.drawable.ic_previous, "BACK!!", PendingIntent.getBroadcast(context, REQUEST_CODE_BACK,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );

                return this;

            case BUTTON_TYPE_END:

                intent.putExtra(MusicService.EXTRA_ACTION, MusicService.ACTION_END);

                notificationBuilder.addAction(
                        R.drawable.ic_close, "CLOSE!!", PendingIntent.getBroadcast(context, REQUEST_CODE_END,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );

        }

        return this;
    }

    public Notification build() {
        return notificationBuilder.build();
    }

    public Notification show() {
        Notification notification = build();
        NotificationManagerCompat.from(context)
                .notify(ID, notification);
        return notification;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = context.getString(R.string.notification_playing_channel_name);
            String description = context.getString(R.string.notification_playing_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            // Create channel
            NotificationChannel channel = new NotificationChannel(CHANNEL, name, importance);
            channel.setDescription(description);
            channel.setBypassDnd(true);

            // Register channel
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public MusicPlayingNotification setTint(int notificationColor) {
        /*notificationBuilder
                .setColorized(true)
                .setColor(notificationColor);*/

        return this;
    }

    public static void cancel(Context context) {
        Log.d("NOTIFICATION", "cancelling all");
        NotificationManagerCompat.from(context)
                .cancelAll();

    }
}
