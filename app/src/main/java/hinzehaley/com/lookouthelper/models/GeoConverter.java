package hinzehaley.com.lookouthelper.models;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.fragments.ShowConversionsFragment;

/**
 * Created by haleyhinze on 6/22/16.
 * Converts lat lng to legal or legal to lat lng. Displays results in a ShowConversionsFragment
 */
public class GeoConverter {

    private ShowConversionsFragment showConversionsFragment;
    private String legal;
    private Location location;

    /**
     * requests the latitude and longitude from a legal description of a geolocation
     *
     * @param state abbreviation. Ex: ID
     * @param meridian
     * @param township int
     * @param range int
     * @param section int
     * @param quarterSection of form NWSE to describe the Northwest corner of the SW quarter
     */
    public void requestLocationFromLegal(ShowConversionsFragment showConversionsFragment, String state, int meridian, int township, int range, int section, String quarterSection, String townshipDirection, String rangeDirection
    , Context context){
        this.showConversionsFragment = showConversionsFragment;
        String url = Constants.GEOCOMMUNICATOR_DOMAIN + Constants.TOWNSHIP_TO_LAT_LON
                + state +"+" + String.format("%02d", meridian) +"+"
                +"T" + String.format("%02d", township) + townshipDirection+ "+"
                + "R" + String.format("%02d", range) +rangeDirection +"+SEC+"
                + String.format("%02d", section) + "+ALIQ+" + quarterSection.toUpperCase() + "&f=pjson";

       legal = "T" + township + townshipDirection + "R" + range +rangeDirection + "S" + section + quarterSection;

        Log.i("URL", " getting text from: " + url);
        VolleyRequester.retrieveText(context, this, url, false, true);

    }

    /**
     * Requests the legal description of a given lat and lng
     * @param showConversionsFragment
     * @param lat
     * @param lon
     */
    public void requestLegalFromLocation(ShowConversionsFragment showConversionsFragment, double lat, double lon, Context context){
        this.showConversionsFragment = showConversionsFragment;
        this.location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lon);

        String url = Constants.GEOCOMMUNICATOR_DOMAIN + Constants.LAT_LON_TO_TOWNSHIP + "lat=" + lat + "&lon=" + lon + Constants.UNITS_AND_FORMAT;

        VolleyRequester.retrieveText(context, this, url, true, false);

    }

    /**
     * Called when a location was retrieved using a legal. Parses the location and passes
     * it into showConversionsFragment to be displayed
     * @param locationString
     */
    public void retrievedLocationString(String locationString){

        location = getLocationFromJson(locationString);

        showConversionsFragment.passInNewLocation(location, legal);
    }

    /**
     * Called when a legal was retrieved using a location. Parses the legal and passes
     * it into showConversionsFragment to be displayed
     * @param legalString
     */
    public void retrievedLegalString(String legalString){
        Log.i("URL", "got legal successfully");

        legal = getLegalFromJson(legalString);
        Log.i("URL", "got legal successfully. It is: " + legal);

        showConversionsFragment.passInNewLegal(location, legal);
    }


    /**
     * Gets the legal description from the json returned when requesting legal from lat lng
     * @param json
     * @return legal, or null if json could not be parsed or does not contain legal
     */
    private String getLegalFromJson(String json){
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        try {
            JSONArray features = jsonObj.getJSONArray("features");

            if(features.length() > 0){
                String data = features.getJSONObject(0).getJSONObject("attributes").getString("landdescription");

                String[] desc = data.split("0(?=[^0])|(?<=[A-Z])(?=[1-9])");
                for(int i = 0; i<desc.length; i++){
                    desc[i] = desc[i].replaceFirst ("^0*", "");
                    if(desc[i].matches("[A-Z]0*")){
                        desc[i] = desc[i].replaceAll("0", "");
                    }
                }

                String quarterSec = "";
                if(desc.length >= 9) {
                    desc[8] = desc[8].replaceFirst("([^SN]*)", "");
                    quarterSec = desc[8];
                }
                String description = "";
                if(desc.length >= 8) {
                    description += "T" + desc[2] + desc[3] + " R" + desc[4] + desc[5] + " Section " + desc[7] + " " + quarterSec;
                }
                return description;
            }

           return null;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Gets the Location from the json returned when requesting lat lng from legal
     * @param json
     * @return Location, or null if json could not be parsed or does not contain lat lng
     */
    private Location getLocationFromJson(String json){
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        try {
            JSONArray coords = jsonObj.getJSONArray("coordinates");

            if(coords.length() > 0){
                JSONObject place = coords.getJSONObject(0);
                double lat = place.getDouble("lat");
                double lon = place.getDouble("lon");
                location = new Location("");
                location.setLatitude(lat);
                location.setLongitude(lon);
                return location;
            }

            return null;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
