package com.maxx.bandcampdemo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.maxx.bandcampdemo.inter.SearchClickListener;
import com.maxx.bandcampdemo.model.SearchResult;

import java.util.ArrayList;

import godau.fynn.bandcampdirect.R;

import static com.maxx.bandcampdemo.Constants.ARTIST_OPEN;
import static com.maxx.bandcampdemo.Constants.BROWSER;
import static com.maxx.bandcampdemo.Constants.DISCOVER;
import static com.maxx.bandcampdemo.Constants.NONE;
import static com.maxx.bandcampdemo.Constants.OPEN;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private ArrayList<SearchResult> searchResultArrayList;
    private Context context;
    private SearchClickListener searchClickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconImageView, actionImageView;
        TextView typeTextView, nameTextView;

        public MyViewHolder(View view) {
            super(view);
            iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
            actionImageView = (ImageView) view.findViewById(R.id.actionImageView);

            typeTextView = (TextView) view.findViewById(R.id.typeTextView);
            nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        }
    }


    public SearchAdapter(Context context, ArrayList<SearchResult> searchResultArrayList, SearchClickListener searchClickListener) {
        this.searchResultArrayList = searchResultArrayList;
        this.context = context;
        this.searchClickListener = searchClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_search, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SearchResult searchResult = searchResultArrayList.get(position);


        holder.typeTextView.setText(searchResult.getClass().getSimpleName());
        holder.nameTextView.setText(searchResult.getTitle());

        int action = searchResult.getAction();

        switch (action) {
            default:
            case NONE:
                holder.actionImageView.setVisibility(View.INVISIBLE);
                // add image as spaceholder
            case OPEN:
            case ARTIST_OPEN:
                holder.actionImageView.setImageDrawable(context.getDrawable(R.drawable.ic_right_24dp));
                break;

            case DISCOVER:
                holder.actionImageView.setImageDrawable(context.getDrawable(R.drawable.ic_search_24dp));
                break;

            case BROWSER:
                holder.actionImageView.setImageDrawable(context.getDrawable(R.drawable.ic_browser_24dp));
                break;
        }

        Glide.with(context).load(searchResult.getCover()).into(holder.iconImageView);

        holder.itemView.setOnClickListener(v -> {
            searchClickListener.onItemClick(searchResult, action);
        });

    }

    @Override
    public int getItemCount() {
        return searchResultArrayList.size();
    }
}