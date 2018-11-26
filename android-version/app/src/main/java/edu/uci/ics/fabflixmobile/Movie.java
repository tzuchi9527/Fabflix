package edu.uci.ics.fabflixmobile;

import java.util.ArrayList;
import android.os.Parcelable;

public class Movie {

    private String title;

    private String director;

    private String year;

    private String id;

    private String genresList;

    private String rating;

    private String starname;



    public Movie(){
    }

    public Movie(String title, String director, String year, String id, String genreList, String starname, String rating) {
        this.title = title;
        this.director = director;
        this.year = year;
        this.id = id;
        this.genresList = genreList;
        this.starname = starname;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenres() {
        return genresList;
    }

    public void setGenres(String genresList) {
        this.genresList = genresList;
    }

    public String getStarname() {
        return starname;
    }

    public void setStarname(String starname) {
        this.starname = starname;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String detail_toString() {
        StringBuffer sb = new StringBuffer();
        //sb.append("Movie Details - \n");
        //sb.append("Id:" + getId());
        //sb.append(", ");
        sb.append(getTitle());
        sb.append("\n\n");
        sb.append("Director : " + getDirector());
        sb.append("\n");
        sb.append("Year : " + getYear());
        sb.append("\n");
        sb.append("Genres : " + getGenres());
        sb.append("\n");
        sb.append("Star : " + getStarname());
        sb.append("\n");
        sb.append("Rating : " + getRating());
        sb.append("\n");
        return sb.toString();
    }
}
