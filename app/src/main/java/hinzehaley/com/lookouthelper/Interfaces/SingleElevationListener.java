package hinzehaley.com.lookouthelper.Interfaces;

import com.android.volley.VolleyError;

import org.json.JSONArray;

/**
 * Created by haleyhinze on 4/28/17.
 */

public interface SingleElevationListener {

    public void singleElevationRetrieved(JSONArray arr);
    public void singleElevationError(VolleyError error);
}
