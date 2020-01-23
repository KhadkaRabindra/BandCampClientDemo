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

import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Giftcard {

    private OkHttpClient client;

    protected Giftcard(OkHttpClient client) {
        this.client = client;
    }

    /**
     * Validates a gift card
     *
     * <br/><br/>Example responses:
     * <br/><br/>
     * <code>{
     *     "ok": false,
     *     "error": "codeInvalid"
     * }</code>
     * <br/><br/>
     * <code>{
     *     "ok": false,
     *     "error": "alreadyRedeemed"
     * }</code>
     * <br/><br/>
     * <code>{"card_data":{"value":{"currency":"EUR","amount":1000,"is_money":true},"card_art_id":5,"email":"user@example.com","delivery":"s","currency":"EUR","img_src":"/img/giftcard/designs/camilla-perkins-02-light.jpg","sender_name":"Max Mustermann","sender_note":null,"card_id":12345,"card_value":1000},"ok":true}</code>
     */
    public JSONObject validate(String code) throws IOException, JSONException {

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"code\":\"" + code +"\",\"skip_redeem\": true}");

        Request request = new Request.Builder()
                .url("https://bandcamp.com/api/giftcard/1/validate")
                .post(body)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        String responseString = client
                .newCall(request).execute()
                .body()
                .string();

        Log.d("VALIDATEGIFT", responseString);

        JSONObject response = new JSONObject(responseString);

        return response;
    }
}
