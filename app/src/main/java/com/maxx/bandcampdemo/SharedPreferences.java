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
import androidx.annotation.Nullable;

import com.maxx.bandcampdemo.model.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferences {
    public static final String IDENTITY_TOKEN = "identityToken";
    android.content.SharedPreferences sharedPreferences;

    public SharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("general", Context.MODE_PRIVATE);
    }

    public String getString(String key, @Nullable String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public android.content.SharedPreferences.Editor edit() {
        return sharedPreferences.edit();
    }

    public boolean has(String key) {
        return sharedPreferences.contains(key);
    }

    public User createUser() {
        return new User(getString(IDENTITY_TOKEN, null));
    }
}
