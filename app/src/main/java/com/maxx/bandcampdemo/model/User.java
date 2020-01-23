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

package com.maxx.bandcampdemo.model;

import androidx.annotation.Nullable;

import com.maxx.bandcampdemo.bandcamp.Bandcamp;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String identityToken;
    private @Nullable String url;
    private @Nullable String displayName;
    private @Nullable String balance;

    public User() {}

    public User(String identityToken) {
        if (identityToken != null) {
            this.identityToken = identityToken;
        }
    }

    public CookieJar makeCookieJar() {
        return new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                ArrayList<Cookie> cookies = new ArrayList<>();

                // Add identity cookie if logged in
                if (identityToken != null && !identityToken.isEmpty()) {
                    Cookie identityCookie = new Cookie.Builder()
                            .domain("bandcamp.com")
                            .name("identity")
                            .value(identityToken)
                            .secure()
                            .path("/")
                            .build();
                    cookies.add(identityCookie);
                }

                return cookies;
            }
        };
    }

    public void update(String html) {
        try {
            JSONObject fanData = Bandcamp.getJSONFromJavaScriptVariables(html, "FanData");

            boolean loggedIn = Bandcamp.getBooleanNullsave(fanData, "logged_in");

            if (loggedIn) {
                displayName = fanData.getString("name");

                Document d = Jsoup.parse(html);

                // Get link to user profile from menu bar
                url = d.getElementById("collection-main").getElementsByAttribute("href").attr("href");

                // Get user balance
                Elements balanceElements = d.getElementsByClass("gift-card-balance");
                if (balanceElements.size() > 0) {
                    balance = balanceElements.text();
                }
            }

        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            // Just don't do it then

            // (don't spam logcat with stack traces)
        }

    }

    public boolean hasBalance() {
        return balance != null;
    }

    @Nullable
    public String getBalance() {
        return balance;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public String getIdentityCookie() {
        return "identity=" + identityToken;
    }
}
