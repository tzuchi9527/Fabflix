package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private ArrayList<Movie> movieList;

    public MovieListViewAdapter(ArrayList<Movie> movieList, Context context) {
        super(context, R.layout.display_list_row, movieList);
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.display_list_row, parent, false);

        Movie movie = movieList.get(position);

        TextView titleView = (TextView)view.findViewById(R.id.title);
        TextView yearView = (TextView)view.findViewById(R.id.year);
        TextView directorView = (TextView)view.findViewById(R.id.director);
        TextView starnameView = (TextView)view.findViewById(R.id.starname);
        TextView genreView = (TextView)view.findViewById(R.id.genre);
        TextView ratingView = (TextView)view.findViewById(R.id.rating);

        titleView.setText(movie.getTitle());
        yearView.setText("Year: " + movie.getYear());
        directorView.setText("Director: " + movie.getDirector());
        starnameView.setText("Stars: " + movie.getStarname());
        genreView.setText("Genres: " + movie.getGenres());
        ratingView.setText("Rating: " + movie.getRating());

        //subtitleView.setText(movie.detail_toString());

        return view;
    }
}