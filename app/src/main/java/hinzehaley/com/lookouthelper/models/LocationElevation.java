package hinzehaley.com.lookouthelper.models;

/**
 * Created by haleyhinze on 4/28/17.
 */

import android.location.Location;

/**
 * Class to represent the location and elevation of a point
 */
public class LocationElevation {
    public Location location;

    //in meters
    public double elevation;

    /**
     *
     * @param location
     * @param elevation in meters
     */
    public LocationElevation(Location location, double elevation){
        this.location = location;
        this.elevation = elevation;
    }
}