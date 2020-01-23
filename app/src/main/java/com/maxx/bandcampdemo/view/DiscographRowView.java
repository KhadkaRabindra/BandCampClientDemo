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
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.maxx.bandcampdemo.activity.AlbumActivity;
import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.model.Artist;


public class DiscographRowView extends RowView {

    private Artist.Discograph discograph;
    private Bandcamp bandcamp;

    public DiscographRowView(Context context, LinearLayout parentView, Artist.Discograph discograph, Bandcamp bandcamp) {

        super(context, parentView, discograph.getTitle(), false, SearchResultRowView.OPEN);

        this.discograph = discograph;
        this.bandcamp = bandcamp;

    }

    @Override
    protected void setOnClickListeners(final Context context, View play, View open, int action) {

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discograph.getUrl(bandcamp, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        String url = msg.getData().getString("url");

                        Intent intent = new Intent(context, AlbumActivity.class);
                        intent.setData(Uri.parse(url));
                        context.startActivity(intent);

                        return false;
                    }
                }), null);
            }
        });

    }
}
