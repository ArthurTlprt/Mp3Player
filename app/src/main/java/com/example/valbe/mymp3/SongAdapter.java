package com.example.valbe.mymp3;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by valbe on 12/11/2017.
 */

public class SongAdapter extends BaseAdapter implements Filterable{

    private ArrayList<Song> songs;
    private ArrayList<Song> songsFilter;
    private LayoutInflater songInf;
    private FriendFilter friendFilter;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        songInf = LayoutInflater.from(c);
        songsFilter = theSongs;
    }

    public ArrayList<Song> getSongs(){
        return songsFilter;
    }

    public void setInitSongList(ArrayList<Song> mySongs){
        songsFilter = mySongs;
    }

    @Override
    public int getCount() {
        return songsFilter.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.song, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        //get song using position
        Song currSong = songsFilter.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

    @Override
    public Filter getFilter() {
        if (friendFilter == null) {
            friendFilter = new FriendFilter();
            Log.e("Filter", "Get Filtering");
        }
        return friendFilter;
    }

    private class FriendFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.e("Filter", constraint.toString());
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<Song> tempList = new ArrayList<Song>();

                // search content in friend list
                for (Song song : songs) {
                    if (song.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        Log.e("Filter", constraint.toString()+" "+song.getTitle());
                        tempList.add(song);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = songs.size();
                filterResults.values = songs;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.e("Filter", constraint.toString() + "final");
            songsFilter = (ArrayList<Song>) results.values;
            notifyDataSetChanged();
        }
    }
}
