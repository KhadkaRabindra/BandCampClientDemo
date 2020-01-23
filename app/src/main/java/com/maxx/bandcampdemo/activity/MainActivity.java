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
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.maxx.bandcampdemo.SharedPreferences;
import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.base.BaseActivity;
import com.maxx.bandcampdemo.discover.Database;
import com.maxx.bandcampdemo.view.RowView;

import godau.fynn.bandcampdirect.R;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_SEARCH = 2;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();

        // Apply correct colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        SharedPreferences sharedPreferences = new SharedPreferences(MainActivity.this);
        final Database database = Database.build(MainActivity.this);


        View search = findViewById(R.id.search);
        search.setOnClickListener((view) -> {

            EditText queryText = new EditText(MainActivity.this);
            queryText.setInputType(InputType.TYPE_CLASS_TEXT);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Search")
                    .setView(queryText)
                    .setPositiveButton("OK", (dialog, which) -> {
                        hideKeyboard();
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_QUERY, queryText.getText().toString());
                        startActivityForResult(intent, REQUEST_SEARCH);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();


        });
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        setTitle("Search");
    }

    public static void downloadCover(Context context, RowView rowView, String cover) {
        Volley.newRequestQueue(context).add(Bandcamp.makeCoverArtRequest(
                cover, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        rowView.addCoverArt(response);
                    }
                }, null));
    }


}
