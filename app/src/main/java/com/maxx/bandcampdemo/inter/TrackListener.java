package com.maxx.bandcampdemo.inter;

import com.maxx.bandcampdemo.model.Track;

public interface TrackListener {
    void onItemClick(Track searchResult, int posiiton);
}
