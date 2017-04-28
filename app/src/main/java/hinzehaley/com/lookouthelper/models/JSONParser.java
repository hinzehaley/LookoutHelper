package hinzehaley.com.lookouthelper.models;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by haleyhinze on 4/28/17.
 */

public class JSONParser {


    public static LocationElevation parseSingleElevation(JSONArray results) throws JSONException {
        try {
            JSONObject elevationObj = results.getJSONObject(0);
            LocationElevation locationElevation = parseJsonLocationElevation(elevationObj);
            return locationElevation;

        } catch (JSONException e) {
            throw e;
        }
    }

    public static LocationElevation parseJsonLocationElevation(JSONObject object) throws JSONException {
        try {
            double elevation = object.getDouble("elevation");
            JSONObject locationObj = object.getJSONObject("location");
            double lat = locationObj.getDouble("lat");
            double lng = locationObj.getDouble("lng");
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);

            return new LocationElevation(location, elevation);

        } catch (JSONException e) {
            throw new JSONException(e.getMessage());
        }

    }
}
