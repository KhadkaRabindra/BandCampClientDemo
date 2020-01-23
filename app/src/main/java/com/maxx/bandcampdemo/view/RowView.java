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
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import godau.fynn.bandcampdirect.R;

public abstract class RowView {

    public static final int NONE = 0;
    public static final int OPEN = 1;
    public static final int DISCOVER = 2;
    public static final int BROWSER = 3;
    public static final int ARTIST_OPEN = 4;

    private Context context;
    private LinearLayout parentView;
    private String name;
    private boolean isPaid;
    private int action;

    private TextView title;
    private TextView paid;
    private ImageButton button;
    private RelativeLayout relativeLayout;
    private ImageView cover;

    @Deprecated
    public RowView(Context context, LinearLayout parentView, String title, boolean paid, boolean mayOpen) {
        this.context = context;
        this.parentView = parentView;
        this.name = title;
        this.isPaid = paid;

        if (mayOpen) {
            action = OPEN;
        }

        createViews();
    }

    public RowView(Context context, LinearLayout parentView, String title, boolean paid, int action) {
        this.context = context;
        this.parentView = parentView;
        this.name = title;
        this.isPaid = paid;
        this.action = action;

        createViews();
    }

    private void createViews() {

        relativeLayout = new RelativeLayout(context);
        parentView.addView(relativeLayout);
        relativeLayout.setPadding(8, 4, 8, 4);

        title = new TextView(context);
        title.setText(name);
        title.setId(R.id.title);
        relativeLayout.addView(title);

        paid = new TextView(context);
        paid.setText("♥");
        paid.setId(R.id.paid);
        relativeLayout.addView(paid);
        if (!isPaid) {
            // title still aligns to it
            paid.setVisibility(View.INVISIBLE);
        }

        button = new ImageButton(context);
        button.setId(R.id.button);
        relativeLayout.addView(button);

        switch (action) {
            default:
            case NONE:
                button.setVisibility(View.INVISIBLE);
                // add image as spaceholder
            case OPEN:
            case ARTIST_OPEN:
                button.setImageDrawable(context.getDrawable(R.drawable.ic_right_24dp));
                break;

            case DISCOVER:
                button.setImageDrawable(context.getDrawable(R.drawable.ic_search_24dp));
                break;

            case BROWSER:
                button.setImageDrawable(context.getDrawable(R.drawable.ic_browser_24dp));
                break;
        }

        // Layout parameters

        // Title
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        titleLayoutParams.addRule(RelativeLayout.LEFT_OF, paid.getId());
        titleLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        title.setLayoutParams(titleLayoutParams);

        // Paid
        RelativeLayout.LayoutParams paidLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        paidLayoutParams.addRule(RelativeLayout.LEFT_OF, button.getId());
        paidLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        paidLayoutParams.setMargins(2, 0, 2, 0);
        paid.setLayoutParams(paidLayoutParams);

        // Button
        RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(paidLayoutParams);
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        button.setLayoutParams(buttonLayoutParams);


        setOnClickListeners(context, relativeLayout, button, action);


    }

    public void implicitlyPaid() {
        //paid.setAlpha(.7f);
        paid.setVisibility(View.VISIBLE);
    }

    protected abstract void setOnClickListeners(Context context, View play, View open, int action);

    public void addCoverArt(Bitmap bitmap) {
        cover = new ImageView(context);
        cover.setImageBitmap(bitmap);
        cover.setId(R.id.cover);
        relativeLayout.addView(cover);

        RelativeLayout.LayoutParams coverLayoutParams = new RelativeLayout.LayoutParams(relativeLayout.getHeight() - 8, relativeLayout.getHeight() - 8);
        coverLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        cover.setLayoutParams(coverLayoutParams);

        cover.setPadding(8, 0, 0, 0);

        title.setPadding(relativeLayout.getHeight() + 16, 0, 0, 0);
    }
}
