package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class login extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }


    public void connectToTomcat(final View view) {

        // Post request form data
        final Map<String, String> params = new HashMap<String, String>();

        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        params.put("username", username);
        params.put("password", password);

        // no user is logged in, so we must connect to the server

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        String mb_ip = getResources().getString(R.string.mobile_ip);
        String url = "https://"+ mb_ip +":8443/fabflix/api/android-login";

        Log.d("url", url);

        // 10.0.2.2 is the host machine when running the android emulator
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("response", response);
                        //((TextView) findViewById(R.id.http_response)).setText(response);
                        // Add the request to the RequestQueue.
                        //queue.add(afterLoginRequest);

                        try{
                            JSONObject resp = new JSONObject(response);
                            String status = resp.getString("status");
                            String message = resp.getString("message");

                            if (status.equals("success")){
                                //success, jump to search page (suppose is blue)
                                Log.d("success_show", status);
                                goToSearch();
                            }else{
                                // fail, display the wrong message on log and the screen
                                Log.d("fail_show", message);
                                ((TextView) findViewById(R.id.http_response)).setText(message);
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                            Log.e("erreurJSON", e.getMessage());
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
        queue.add(loginRequest);
    }

    public void goToSearch() {
        Intent goToIntent = new Intent(this, main_search.class);
        startActivity(goToIntent);
    }
}
