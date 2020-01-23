package com.maxx.bandcampdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maxx.bandcampdemo.inter.ArtistClickListener;
import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.Artist;

import java.util.ArrayList;

import godau.fynn.bandcampdirect.R;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.MyViewHolder> {

    private ArrayList<Artist.Discograph> trackList;
    private Context context;
    private ArtistClickListener trackListener;

    public ArtistAdapter(Context applicationContext, ArrayList<Artist.Discograph> discography, ArtistClickListener artistClickListener) {
        this.trackList = discography;
        this.context = applicationContext;
        this.trackListener = artistClickListener;
    }

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

    @Override
    public ArtistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_artist, parent, false);

        return new ArtistAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArtistAdapter.MyViewHolder holder, int position) {
        Artist.Discograph track = trackList.get(position);

        holder.nameTextView.setText(track.getTitle());

        Glide.with(context).load(track.getCover(0)).into(holder.iconImageView);

        holder.itemView.setOnClickListener(v -> {
            trackListener.onItemClick(track);
        });


    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }
}
