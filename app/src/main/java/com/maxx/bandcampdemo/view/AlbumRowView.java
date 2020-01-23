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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import com.maxx.bandcampdemo.activity.AlbumActivity;
import com.maxx.bandcampdemo.model.Album;

public class AlbumRowView extends RowView {

    private Album album;

    private Handler onLongClick;

    public AlbumRowView(Context context, LinearLayout parentView, Album album, boolean mayOpen) {
        super(context, parentView, album.getTitle(), album.isPaid(), mayOpen);

        if (album.isImplicitlyPaid()) {
            implicitlyPaid();
        }

        this.album = album;
    }

    public AlbumRowView(Context context, LinearLayout parentView, Album album, boolean mayOpen, Handler onLongClick) {
        super(context, parentView, album.getTitle(), album.isPaid(), mayOpen);

        if (album.isImplicitlyPaid()) {
            implicitlyPaid();
        }

        this.album = album;

        this.onLongClick = onLongClick;
    }

    @Override
    protected void setOnClickListeners(final Context context, View play, View open, int action) {
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlbumActivity.play(context, album, 0);
            }
        });

        if (action == RowView.OPEN) {
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    intent.setData(Uri.parse(album.getUrl()));

                    intent.putExtra(AlbumActivity.EXTRA_ALBUM, album);
                    context.startActivity(intent);
                }
            });
        }

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongClick != null) {
                    onLongClick.sendEmptyMessage(0);
                }
                return true;
            }
        };

        if (open.getVisibility() == View.VISIBLE) {
            open.setOnLongClickListener(onLongClickListener);
        } else {
            play.setOnLongClickListener(onLongClickListener);
        }
    }
}
