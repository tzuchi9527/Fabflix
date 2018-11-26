package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class main_search extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void connectToTomcatSearch(final View view) {

        // Post request form data
        final Map<String, String> params = new HashMap<String, String>();

        String title = ((EditText) findViewById(R.id.title)).getText().toString();
        String year = ((EditText) findViewById(R.id.year)).getText().toString();
        String director = ((EditText) findViewById(R.id.director)).getText().toString();
        String starname = ((EditText) findViewById(R.id.starname)).getText().toString();

        params.put("title", title);
        params.put("year", year);
        params.put("director", director);
        params.put("starname", starname);

        System.out.println("title here: "+ title);

        String[] split_title = title.split("\\s+");
        String title_match = "";
        for (int i=0; i<split_title.length; i++) {
            String temp = split_title[i] + "%20";
            title_match += temp;
        }

        System.out.println("title here: "+ title_match);

        String page = "1";
        String numOfpage = "1000";
        String sort = "title";
        String order = "ASC";

        String mb_ip = getResources().getString(R.string.mobile_ip);
        String url = "https://"+ mb_ip +":8443/fabflix/search?page="+ page + "&numOfpage=" + numOfpage +
                "&sort=" + sort + "&order=" + order + "&title=" + title_match +
                "&year=" + year + "&director=" + director + "&starname=" + starname;

        Log.d("title", title);
        Log.d("year", year);
        Log.d("director", director);
        Log.d("starname", starname);

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // 10.0.2.2 is the host machine when running the android emulator
        final StringRequest searchRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);

                        ArrayList<Movie> movieList = new ArrayList<>();

                        if (response.equals("[]") || response.equals("")){
                            ((TextView) findViewById(R.id.http_response)).setText("NO RESULTS");
                        }
                        else {
                            ((TextView) findViewById(R.id.http_response)).setText("");
                            try {
                                JSONArray resp = new JSONArray(response);

                                // use JSONArray type as String type and send it to Display_List page
                                goToDisplayList(response);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("erreurJSON", e.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("security.error", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }  // HTTP POST Form Data
        };
        queue.add(searchRequest);
    }

    public void goToDisplayList(String Movie) {
        Intent goToIntent = new Intent(this, Display_List.class);
        goToIntent.putExtra("MovieList_String", Movie);
        startActivity(goToIntent);
    }
}