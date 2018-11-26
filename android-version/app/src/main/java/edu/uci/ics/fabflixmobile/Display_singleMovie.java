package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Display_singleMovie extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_singlemovie);

        Bundle bundle = getIntent().getExtras();

        String msg = bundle.getString("Movie_String");

        ((TextView) findViewById(R.id.movie_data)).setText(msg);
    }
}
