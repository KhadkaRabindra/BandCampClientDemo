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

package com.maxx.bandcampdemo.bandcamp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.android.volley.toolbox.ImageRequest;
import com.maxx.bandcampdemo.model.Artist;
import com.maxx.bandcampdemo.model.User;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Bandcamp {

    public static final String ARTIST = "artist";

    private static final String IMAGE_PREFIX = "https://f4.bcbits.com/img/";
    private static final String IMAGE_ALBUM_PREFIX = "https://f4.bcbits.com/img/a";
    private static final String IMAGE_POSTFIX = "_0";

    private OkHttpClient client;
    private User user;

    public Bandcamp(User user) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (user != null) {
            this.user = user;
            builder.cookieJar(user.makeCookieJar());
        }

        client = builder.build();
    }

    /** Download website from bandcamp by url<br/><br/>
     * <p>Use the provided client to request url, calling success on success and failure on failure</p>
     * */
    public void request(String url, @Nullable final Handler success, @Nullable final Handler failure) {
        //

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (failure != null) {
                    failure.sendEmptyMessage(0);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();

                // Update user data
                if (user != null) {
                    user.update(body);
                }

                if (success != null) {
                    // …four lines to pack a message?
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("html", body);
                    message.setData(bundle);
                    success.sendMessage(message);
                }


            }
        });
    }

    /** Download website from bandcamp by url <b>synchronously</b>
     * @return Response gathered*/
    public String requestSynchronously(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        return client.newCall(request).execute().body().string();
    }

    public static ImageRequest makeCoverArtRequest(String coverUrl, com.android.volley.Response.Listener<Bitmap> listener,
                                                   com.android.volley.Response.ErrorListener errorListener) {
        ImageRequest imageRequest = new ImageRequest(coverUrl, listener, 0, 0,
                ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565, errorListener);
        imageRequest.setShouldCache(true);
        return imageRequest;
    }


    public static boolean getBooleanNullsave(JSONObject jsonObject, String name) {
        try {
            return jsonObject.getBoolean(name);
        } catch (JSONException e) {
            return false;
        }
    }

    public static String getHtmlFromMessage(Message message) {
        return message.getData().getString("html");
    }

    public static JSONObject getJSONFromJavaScriptVariables(String html, String variable) throws JSONException{
        // We only want the JSON behind "var $variable"


        String[] part = html.split("var " + variable + " = ");

        String firstHalfGone = part[1];

        firstHalfGone = firstHalfGone.replaceAll("\" \\+ \"", "");

        int position = -1;
        int level = 0;
        for (char character : firstHalfGone.toCharArray()) {
            position++;

            switch (character) {
                case '{':
                    level++;
                    continue;
                case '}':
                    level--;
                    if (level == 0) {
                        return new JSONObject(firstHalfGone.substring(0, position + 1));
                    }
                    continue;
            }
        }

        throw new JSONException("Not closed");

        /*var FanData = {
    logged_in: false,
    name: null,
    image_id: null
};*/
    }

    /** Nothing is actually posted here, it's just an HTTP POST query. */
    public void postArtistDetails(long intExtra, @Nullable final Handler okay, @Nullable final Handler failure) {
        final String URL = "https://bandcamp.com/api/mobile/22/band_details";


        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"band_id\":\"" + intExtra + "\"}");
        Request request = new Request.Builder()
                .url("https://bandcamp.com/api/mobile/22/band_details")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (failure != null) {
                    failure.sendEmptyMessage(0);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();

                if (okay != null) {
                    try {
                        // …four lines to pack a message?
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ARTIST, new Artist(new JSONObject(body)));
                        message.setData(bundle);
                        okay.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();

                        if (failure != null) {
                            failure.sendEmptyMessage(0);
                        }
                    }
                }
            }
        });





    }

    public Giftcard buildGiftcard() {
        return new Giftcard(client);
    }

    public static String getArtistImageUrl(long id) {
        if (id != 0) {
            return IMAGE_PREFIX + id + IMAGE_POSTFIX;
        } else {
            return null;
        }
    }

    public static String getAlbumImageUrl(long id, int size) {
        if (id != 0) {
            return IMAGE_ALBUM_PREFIX + id + "_" + size;
        } else {
            return null;
        }
    }
}
