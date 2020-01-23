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
import android.view.View;
import android.widget.LinearLayout;

import com.maxx.bandcampdemo.activity.AlbumActivity;
import com.maxx.bandcampdemo.activity.ArtistActivity;
import com.maxx.bandcampdemo.model.SearchResult;

public class SearchResultRowView extends RowView {

    private SearchResult result;

    public SearchResultRowView(Context context, LinearLayout parentView, SearchResult searchResult) {
        super(context, parentView, searchResult.getClass().getSimpleName() + ": " + searchResult.getTitle(), false, searchResult.getAction());

        result = searchResult;
    }

    @Override
    protected void setOnClickListeners(final Context context, View play, View open, int action) {
        if (action == RowView.OPEN) {

            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    intent.setData(Uri.parse(result.getUrl()));

                    context.startActivity(intent);

                }
            });
        } else if (action == RowView.ARTIST_OPEN) {

            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ArtistActivity.class);
                    intent.putExtra(ArtistActivity.ARTIST_ID, Long.parseLong(result.getUrl()));
                    context.startActivity(intent);
                }
            });

        } else if (action == RowView.DISCOVER) {


        }
    }
}
