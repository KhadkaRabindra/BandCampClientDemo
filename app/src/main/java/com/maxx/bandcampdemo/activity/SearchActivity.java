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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxx.bandcampdemo.SharedPreferences;
import com.maxx.bandcampdemo.adapters.SearchAdapter;
import com.maxx.bandcampdemo.bandcamp.Bandcamp;
import com.maxx.bandcampdemo.base.BaseActivity;
import com.maxx.bandcampdemo.inter.SearchClickListener;
import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.Artist;
import com.maxx.bandcampdemo.model.Fan;
import com.maxx.bandcampdemo.model.SearchResult;
import com.maxx.bandcampdemo.model.Track;
import com.maxx.bandcampdemo.view.RowView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import godau.fynn.bandcampdirect.R;

public class SearchActivity extends BaseActivity {
    RecyclerView recyclerView;

    public static final String EXTRA_QUERY = "query";
    private static final String SEARCH_URL = "https://bandcamp.com/search?q=";

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpToolbar();
        recyclerView = (RecyclerView) findViewById(R.id.searchRecyclerView);

        performSearch();
    }

    private void performSearch() {
        showProgressDialog();
        SharedPreferences sharedPreferences = new SharedPreferences(SearchActivity.this);
        Bandcamp bandcamp = new Bandcamp(sharedPreferences.createUser());
        String query = getIntent().getStringExtra(EXTRA_QUERY);
        bandcamp.request(makeRequestUrl(query), new Handler((message) -> {
            dismissProgressDialog();
            handleSearchSuccess(message);
            return false;
        }), new Handler(msg -> {
            return false;
        }));
    }

    private void handleSearchSuccess(Message message) {
        String html = Bandcamp.getHtmlFromMessage(message);

        // Parse search results

        ArrayList<SearchResult> searchResults = new ArrayList<>();

        try {

            Document d = Jsoup.parse(html);

            Elements searchResultsElements = d.getElementsByClass("searchresult");

            for (Element searchResult :
                    searchResultsElements) {

                Element resultInfo = searchResult.getElementsByClass("result-info").first();

                String type = resultInfo
                        .getElementsByClass("itemtype").first().text();

                String image = null;
                Element img = searchResult.getElementsByClass("art").first()
                        .getElementsByTag("img").first();
                if (img != null) {
                    image = img.attr("src");
                }

                String heading = resultInfo.getElementsByClass("heading").text();

                String subhead = resultInfo.getElementsByClass("subhead").text();

                String url = resultInfo.getElementsByClass("itemurl").text();

                switch (type) {
                    default:
                        continue;
                    case "FAN":
                        searchResults.add(new Fan(heading, url, image));
                        break;

                    case "ARTIST":
                        String id = resultInfo.getElementsByClass("itemurl").first()
                                .getElementsByTag("a").first()
                                .attr("href") // the link contains the id
                                .split("search_item_id=")
                                [1] // the number is behind its name
                                .split("&") // there is another attribute behind the name
                                [0]; // get the number

                        searchResults.add(new Artist(heading, Long.parseLong(id), image, subhead));
                        break;

                    case "ALBUM":
                        String artist = subhead.split(" by")[0];
                        searchResults.add(new Album(heading, artist, url, image));
                        break;

                    case "TRACK":
                        String album = subhead.split("from ")[0].split(" by")[0];

                        String[] splitBy = subhead.split(" by");
                        String artist1 = null;
                        if (splitBy.length > 1) {
                            artist1 = subhead.split(" by")[1];
                        }
                        searchResults.add(new Track(heading, url, image, artist1, album));
                        break;
                }

            }

            SearchAdapter adapter = new SearchAdapter(getApplicationContext(), searchResults, new SearchClickListener() {
                @Override
                public void onItemClick(SearchResult searchResult, int action) {
                    if (action == RowView.OPEN) {

                        Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                        intent.setData(Uri.parse(searchResult.getUrl()));
                        startActivity(intent);
                    } else if (action == RowView.ARTIST_OPEN) {
                        Intent intent = new Intent(getApplicationContext(), ArtistActivity.class);
                        intent.putExtra(ArtistActivity.ARTIST_ID, Long.parseLong(searchResult.getUrl()));
                        startActivity(intent);
                    } else if (action == RowView.DISCOVER) {

                    }
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);

            setResult(RESULT_OK);

        } catch (Exception e) {
            dismissProgressDialog();
            e.printStackTrace();
        }
    }

    private String makeRequestUrl(String query) {

        query = query.replaceAll("%", "%25");
        query = query.replaceAll(" ", "%20");
        query = query.replaceAll("#", "%23");
        query = query.replaceAll("\\$", "%24");
        query = query.replaceAll("&", "%26");
        query = query.replaceAll("'", "%27");

        return SEARCH_URL + query;
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        setTitle("Search Result");
    }
}
