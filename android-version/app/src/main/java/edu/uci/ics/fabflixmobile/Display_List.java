package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Display_List extends Activity {

    private ListView listView;
    private TextView title;
    private Button btn_prev;
    private Button btn_next;

    private int pageCount;

    private ArrayList<Movie> movieList;
    MovieListViewAdapter adapter;

    private int increment = 0; // Using this increment value we can move the listview items
    public int TOTAL_LIST_ITEMS;
    public int NUM_ITEMS_PAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_list);

        // get movie list result
        Bundle bundle = getIntent().getExtras();
        String response = bundle.getString("MovieList_String");

        Log.d("movieList_String", response);
        movieList = new ArrayList<>();

        // change to ArrayList<Movie>
        try {
            JSONArray resp = new JSONArray(response);

            for (int i = 0; i < resp.length(); i++){
                JSONObject movie= resp.getJSONObject(i);
                String rs_id = movie.getString("m_id");
                String rs_title = movie.getString("m_title");
                String rs_year = movie.getString("m_year");
                String rs_director = movie.getString("m_director");
                String rs_starname = movie.getString("s_starname");
                //String rs_star_id = movie.getString("s_id");
                String rs_genre = movie.getString("g_name");
                String rs_rating = movie.getString("r_rating");

                Movie m_tmp = new Movie();
                m_tmp.setDirector(rs_director);
                m_tmp.setId(rs_id);
                m_tmp.setGenres(rs_genre);
                m_tmp.setYear(rs_year);
                m_tmp.setTitle(rs_title);
                m_tmp.setStarname(rs_starname);
                m_tmp.setRating(rs_rating);

                movieList.add(m_tmp);
            }
            TOTAL_LIST_ITEMS = resp.length();
            System.out.println(movieList);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("erreurJSON", e.getMessage());
        }

        listView = (ListView)findViewById(R.id.list);
        btn_prev = (Button)findViewById(R.id.prev);
        btn_next = (Button)findViewById(R.id.next);
        title = (TextView)findViewById(R.id.numOfpage);

        // check the number of pages
        int val = TOTAL_LIST_ITEMS % NUM_ITEMS_PAGE;
        val = val==0?0:1;
        pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE + val;

        btn_prev.setEnabled(false);
        if (pageCount==1){
            btn_next.setEnabled(false);
        }

        loadList(0);

        btn_next.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                increment++;
                loadList(increment);
                CheckEnable();
            }
        });

        btn_prev.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                increment--;
                loadList(increment);
                CheckEnable();
            }
        });
    }

    // Method for enabling and disabling Buttons
    private void CheckEnable()
    {
        btn_prev.setEnabled(true);
        btn_next.setEnabled(true);

        if(increment+1 == pageCount)
        {
            btn_next.setEnabled(false);
        }
        if(increment == 0)
        {
            btn_prev.setEnabled(false);
        }
    }

    /**
     * Method for loading data in listview
     * @param number
     */
    private void loadList(int number)
    {
        ArrayList<Movie> sort = new ArrayList<>();
        title.setText("Page "+(number+1)+" of "+pageCount);

        int start = number * NUM_ITEMS_PAGE;
        for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++)
        {
            if(i < movieList.size())
            {
                sort.add(movieList.get(i));
            }
            else
            {
                break;
            }
        }

        adapter = new MovieListViewAdapter(sort, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Person person = people.get(position);
                //String message = String.format("Clicked on position: %d, name: %s, %d", position, person.getName(), person.getBirthYear());
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Movie single_movie = movieList.get(position);
                goToSingleMovie(single_movie);

            }
        });

    }

    public void goToSingleMovie(Movie singleMovie) {
        Intent goToIntent = new Intent(this, Display_singleMovie.class);
        goToIntent.putExtra("Movie_String", singleMovie.detail_toString());
        startActivity(goToIntent);
    }

}

