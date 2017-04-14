package hinzehaley.com.lookouthelper.models;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.fragments.ShowConversionsFragment;

/**
 * Created by haleyhinze on 6/22/16.
 */
public class RetrieveTextFromURL extends AsyncTask<String, String, String> {

    private GeoConverter geoConverter;
    String fullText = "";
    String url = "";
    boolean isLegal = false;
    boolean isLatLon = false;



    public RetrieveTextFromURL(GeoConverter geoConverter, boolean isLatLon, boolean isLegal){
        this.geoConverter = geoConverter;
        this.isLatLon = isLatLon;
        this.isLegal = isLegal;
    }

    @Override
    protected String doInBackground(String... strings) {

        try{
            URL url = new URL(strings[0]);
            this.url = strings[0];
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            Log.i("url", url.toString());
            String line;
            while((line = in.readLine()) != null){
                fullText += line;
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i("GEOLOCATION", "retrieved text. OnPostExecute " + fullText);
        if(isLegal) {
            geoConverter.retrievedLocationString(fullText);
        }else{
            geoConverter.retrievedLegalString(fullText);
        }
    }
}
