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


import com.maxx.bandcampdemo.view.RowView;

public class Fan implements SearchResult {

    private String name;
    private String url;
    private String image;

    public Fan(String name, String url, String image) {
        this.name = name;
        this.url = url;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getCover() {
        return getImage();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public String getImage() {
        return image;
    }

    public int getAction() {
        return RowView.DISCOVER;
    }
}
