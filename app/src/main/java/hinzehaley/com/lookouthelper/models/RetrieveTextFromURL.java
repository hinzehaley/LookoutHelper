package hinzehaley.com.lookouthelper.models;

import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.THREAD_PRIORITY_DISPLAY;
import static android.os.Process.THREAD_PRIORITY_FOREGROUND;
import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;


/**
 * Created by haleyhinze on 6/22/16.
 * Class to get text at a URL
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

    /**
     * Retrieves text at url.
     * Currently, this background task has a priority set to almost display priority. This could make the UI slower,
     * but it is set this way because it is a fast background task. Without prioritizing it highly, it doesn't
     * execute quickly enough because loading the map takes so long.
     * @param strings
     * @return
     */
    @Override
    protected String doInBackground(String... strings) {
        Process.setThreadPriority(THREAD_PRIORITY_FOREGROUND + THREAD_PRIORITY_MORE_FAVORABLE);
        try{
            Log.i("URL", "retrieving text");
            URL url = new URL(strings[0]);
            this.url = strings[0];
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
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

    /**
     * When text has been retrieved, pass it into GeoConverter
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        Log.i("URL", "onPostExecute");

        super.onPostExecute(s);
        if(isLegal) {
            Log.i("URL", "is legal");

            geoConverter.retrievedLocationString(fullText);
        }else{
            Log.i("URL", "not legal");

            geoConverter.retrievedLegalString(fullText);
        }
    }
}
