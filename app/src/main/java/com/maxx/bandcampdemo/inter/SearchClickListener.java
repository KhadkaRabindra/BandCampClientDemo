package com.maxx.bandcampdemo.inter;


import com.maxx.bandcampdemo.model.SearchResult;


public interface SearchClickListener {
    void onItemClick(SearchResult searchResult, int action);
}