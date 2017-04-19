package hinzehaley.com.lookouthelper.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.fragments.MapReportFragment;

/**
 * Created by haleyhinze on 6/24/16.
 * Class to request elevation data from google elevation api using GET request
 */
public class VolleyRequester {

    static RequestQueue queue = null;


    public void requestElevation(double startLat, double startLon, double endLat, double endLon, final Context context, final MapReportFragment callerFragment, int numSamples, final int resultNum) {

        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(15);

        String startLatStr = df.format(startLat);
        String startLonStr = df.format(startLon);
        String endLatStr = df.format(endLat);
        String endLonStr = df.format(endLon);

        //Builds request URLs
        String urlPath = "https://maps.googleapis.com/maps/api/elevation/json?path=" + startLatStr + "," + startLonStr + "|" + endLatStr + "," + endLonStr + "&samples=" + numSamples + "&key=" + context.getString(R.string.google_elevation_key);
        String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + startLatStr + "," + startLonStr + "&key=" + context.getString(R.string.google_elevation_key);

        // Instantiate the RequestQueue.
        if(queue == null) {
            queue = Volley.newRequestQueue(context);
        }


    // Request a string response from the provided URL. Passes reponse into callerFragment
    StringRequest stringRequest = new StringRequest(Request.Method.GET, urlPath,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray arr = jsonObject.getJSONArray("results");
                        callerFragment.elevationResult(arr, resultNum);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Display the first 500 characters of the response string.

                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("VOLLEY", "error response is: " + error.getMessage());

            //TODO: handle error response
            callerFragment.elevationResult(null, resultNum);

        }
    });

        //15000 milliseconds to timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    // Add the request to the RequestQueue. Does not cache results as lots of data is being requested
        stringRequest.setShouldCache(false);
    queue.add(stringRequest);
}
}
