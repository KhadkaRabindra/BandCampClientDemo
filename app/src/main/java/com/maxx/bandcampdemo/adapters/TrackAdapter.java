package com.maxx.bandcampdemo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.maxx.bandcampdemo.inter.TrackListener;
import com.maxx.bandcampdemo.model.Album;
import com.maxx.bandcampdemo.model.Track;

import java.util.ArrayList;

import godau.fynn.bandcampdirect.R;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.MyViewHolder> {

    private ArrayList<Track> trackList;
    private Context context;
    private TrackListener trackListener;
    private Album album;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;

        public MyViewHolder(View view) {
            super(view);

            nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        }
    }


    public TrackAdapter(Context context, ArrayList<Track> trackList, Album album, TrackListener trackListener) {
        this.trackList = trackList;
        this.context = context;
        this.trackListener = trackListener;
        this.album = album;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_track, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Track track = trackList.get(position);

        Gson gson = new Gson();
        Log.i("TRACK", gson.toJson(track));

        holder.nameTextView.setText(track.getTitle());

        holder.itemView.setOnClickListener(v -> {
            trackListener.onItemClick(track, position);
        });

    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }
}
