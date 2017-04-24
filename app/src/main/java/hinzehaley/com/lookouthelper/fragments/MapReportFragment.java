package hinzehaley.com.lookouthelper.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.PreferencesKeys;
import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.models.GeoConverter;
import hinzehaley.com.lookouthelper.models.VolleyRequester;

/**
 * Fragment to display a map showing the line from the lookout and best guesses of
 * fire location along the line. Fire location markers are color coded, with green being
 * the most likely option and red being the least likely option.
 *
 * When a marker is clicked, updates a ShowConversionsFragment on the bottom of the screen to
 * reflect markers location. Additionally, displays a window by the marker saying how much higher
 * or lower the actual elevation of that geo point is than the elevation it should be based on
 * calculations from azimuth and vertical angle.
 */
public class MapReportFragment extends Fragment implements OnMapReadyCallback {

    //args keys
    private static final String SMOKE_COLOR = "smokeColor";
    private static final String CROSS_LOOKOUT = "crossLookout";
    private static final String LANDMARK = "landmark";
    private static final String ADDITIONAL_INFO = "additionalInfo";
    private static final String BASE_VISIBLE = "baseVisible";
    private static final String HAVE_CROSS = "haveCross";
    private static final String HORIZONTAL_AZIMUTH = "horizontalAzimuth";
    private static final String VERTICAL_AZIMUTH = "verticalAzimuth";
    private static final String CROSS_AZIMUTH = "crossAzimuth";

    private Marker curClickMarker;
    private VolleyRequester volleyRequester;

    private Location lookoutLocation;


    boolean[] retrievalArray = new boolean[Constants.NUM_ELEVATION_REQUESTS];
    private ArrayList<JSONArray> resultArrays = new ArrayList<>(Constants.NUM_ELEVATION_REQUESTS);

    private ArrayList<LocationElevation> elevationArr;

    private String smokeColor;
    private String crossLookout;
    private String landmark;
    private String additionalInfo;
    private boolean baseVisible;
    private boolean haveCross;
    private float horizontalAzimuth;
    private float verticalAzimuth;
    private float crossAzimuth;

    private View v;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    ShowConversionsFragment showConversionsFragment;
    private SharedPreferences prefs;

    private Spinner mapTypeSpinner;
    private int mapType = 1;

    ArrayList<LocationElevation> matches = new ArrayList<LocationElevation>();
    String[] mapTypes = new String[4];


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment MapReportFragment.
     */
    public static MapReportFragment newInstance(boolean baseVisible, boolean haveCross, float horizontalAzimuth,
                                                float verticalAzimuth, String crossLookout, float crossAzimuth,
                                                String smokeColor, String landmark, String additionalInfo) {
        MapReportFragment fragment = new MapReportFragment();
        Bundle args = new Bundle();
        args.putBoolean(BASE_VISIBLE, baseVisible);
        args.putBoolean(HAVE_CROSS, haveCross);
        args.putFloat(HORIZONTAL_AZIMUTH, horizontalAzimuth);
        args.putFloat(VERTICAL_AZIMUTH, verticalAzimuth);
        args.putString(CROSS_LOOKOUT, crossLookout);
        args.putFloat(CROSS_AZIMUTH, crossAzimuth);
        args.putString(SMOKE_COLOR, smokeColor);
        args.putString(LANDMARK, landmark);
        args.putString(ADDITIONAL_INFO, additionalInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public MapReportFragment() {
        // Required empty public constructor
    }

    /**
     * gets information from arguments
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            baseVisible = getArguments().getBoolean(BASE_VISIBLE);
            haveCross = getArguments().getBoolean(HAVE_CROSS);
            horizontalAzimuth = getArguments().getFloat(HORIZONTAL_AZIMUTH);
            crossLookout = getArguments().getString(CROSS_LOOKOUT);
            crossAzimuth = getArguments().getFloat(CROSS_AZIMUTH);
            verticalAzimuth = getArguments().getFloat(VERTICAL_AZIMUTH);
            smokeColor = getArguments().getString(SMOKE_COLOR);
            landmark = getArguments().getString(LANDMARK);
            additionalInfo = getArguments().getString(ADDITIONAL_INFO);
        }
    }

    /**
     * Sets up view by showing conversions fragment and filling map_container with a SupportMapFragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_map_report, container, false);

        prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);

        FragmentManager fm = getChildFragmentManager();
        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mMap == null) {
            mMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mMapFragment).commit();
        }
        showConversionsFragment();
        initilizeMap();

        Log.e("CROSS", "weird bug");
        Log.i("CROSS", "weird bug");


        mapTypes = new String[4];
        mapTypes[0] = "Satellite Map";
        mapTypes[1] = "Road Map";
        mapTypes[2] = "Terrain Map";
        mapTypes[3] = "Hybrid Map";
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapTypeSpinner = (Spinner) view.findViewById(R.id.spinner_map_type);


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.spinner_item, mapTypes);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mapTypeSpinner.setAdapter(dataAdapter);
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mapType = position;
                updateMapType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void updateMapType(){
        if (mapType == 0){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } if (mapType == 1){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } if (mapType == 2){
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } if (mapType == 3){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }

    /**
     * Initializes map
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mMap == null) {
            initilizeMap();

        }
    }


    private void initilizeMap() {
        if (mMap == null) {
            mMapFragment.getMapAsync(this);
        }


    }

    /**
     * Called when map is ready. Sets the map type, listens for marker clicks and shows
     * marker information and location information when a marker is clicked.
     * @param map
     */
    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        updateMapType();

        //Shows user location
        if(checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        }

        //Allows user to zoom
        map.getUiSettings().setZoomControlsEnabled(true);

        calculatePossibleFireLocation();

        //When marker is clicked, shows a ShowConversionsFragment with location info
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng conversionPoint = marker.getPosition();
                showConversionsFragment(conversionPoint);

                return false;
            }
        });

        // Sets the info window adapter to display both title and snippet
        // in the info window for a marker
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });



        //listens for map clicks
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (!haveCross) {

                    LatLng nearestPointOnLine = findNearestLinePoint(latLng, horizontalAzimuth);
                    if (curClickMarker != null) {
                        curClickMarker.remove();
                    }

                    curClickMarker = createMarker(getString(R.string.clicked_location), "", BitmapDescriptorFactory.HUE_VIOLET, nearestPointOnLine);
                    showConversionsFragment(nearestPointOnLine);
                    getElevationOfPoint(nearestPointOnLine);
                }
            }
        });

    }

    /**
     * finds the point on the line that is nearest to clickedPoint
     *
     * @param clickedPoint
     * @return LatLng nearestLinePoint
     */
    private LatLng findNearestLinePoint(LatLng clickedPoint, float horizontalAzimuth){
        double distance = findDistanceToPoint(clickedPoint);
        return calculateLinePoint((float)distance, horizontalAzimuth,  new LatLng(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0),
                prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0)));
    }

    /**
     * finds the distance to a point from the lookout location
     *
     * @param point
     * @return double distance
     */
    private double findDistanceToPoint(LatLng point){
        double distance = com.google.maps.android.SphericalUtil.computeDistanceBetween(
                new LatLng(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0),
                        prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0)), point);
        return distance;
    }


    /**
     * Calculates fire locations. Different calculations are done if there is
     * no cross v if there is a cross
     */
    //TODO: don't yet have option for if base is not visible.
    private void calculatePossibleFireLocation(){

        if(!haveCross){
            noCrossVisibleBase();
        }else if(haveCross){
            haveCross();
        }
    }


    /**
     * Calculates fire location by getting point where azimuth from cross lookout and your
     * own lookout intersect
     */
    private void haveCross(){

        LatLng fireLocation = null;
        LatLng yourLocation = new LatLng(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0),
                prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0));
        float crossLat = prefs.getFloat(crossLookout + "lat", 0);
        float crossLon = prefs.getFloat(crossLookout + "lon", 0);
        LatLng crossLocation = new LatLng(crossLat, crossLon);

        //gets azimuth from you to the cross lookout
        double azimuthToCross = SphericalUtil.computeHeading(yourLocation, crossLocation);

        //gets azimuth from cross lookout to your lookout
        double azimuthFromCross = SphericalUtil.computeHeading(crossLocation, yourLocation);

        //Corrects for azimuths that are not in valid degree range
        if(azimuthToCross < 0){
            azimuthToCross = 360 + azimuthToCross;
        }if(azimuthFromCross < 0){
            azimuthFromCross = 360 + azimuthFromCross;
        }

        float angleYou;

        //gets angle between crossAzimuth and fire azimuth
        if(horizontalAzimuth < azimuthToCross){
            angleYou = (float) (azimuthToCross - horizontalAzimuth);
        }else{
            angleYou = (float) (horizontalAzimuth - azimuthToCross);
        }

        float angleCross;

        //gets angle between azimuth reported by cross and azimuth to your lookout from cross lookout
        if(crossAzimuth > azimuthFromCross){
            angleCross = (float) (crossAzimuth - azimuthFromCross);
        }else{
            angleCross = (float) (azimuthFromCross - crossAzimuth);
        }

        //Has calculations of two other degrees in the triangle, uses this to get the angle between the azimuth
        //from the fire to you and the azimuth from the fire to the cross. In a triangle, all angles add up
        //to 180, so just subtract the other angles from 180
        float angleFire = 180 - angleYou - angleCross;

        //gets distance from you to cross
        double distanceToCross = SphericalUtil.computeDistanceBetween(yourLocation, crossLocation);

        //gets distance from you to fire by using sin function using the angle to the cross, the angle to the fire, and the
        //distance to the cross
        double distanceFromYouToFire = Math.abs((distanceToCross * Math.sin(Math.toRadians(angleCross)))/Math.sin(Math.toRadians(angleFire)));

        //Gets the fire location by computing the offset from your location out along your azimuth by the distance
        //calculated above
        fireLocation = SphericalUtil.computeOffset(yourLocation, distanceFromYouToFire, horizontalAzimuth);

        //Creates marker showing fire location
        createMarker(getString(R.string.fire_location), "", BitmapDescriptorFactory.HUE_VIOLET, fireLocation);

        //Creates endpoints for lines to display on map showing azimuths from your lookout and the cross lookout
        LatLng endOfYourAzimuth = SphericalUtil.computeOffset(yourLocation, Constants.MAX_ELEVATOIN_REQUEST_DISTANCE, horizontalAzimuth);
        LatLng endOfCrossAzimuth = SphericalUtil.computeOffset(crossLocation, Constants.MAX_ELEVATOIN_REQUEST_DISTANCE, crossAzimuth);

        //Draws azimuth lines on map
        drawLine(yourLocation, endOfYourAzimuth);
        drawLine(crossLocation, endOfCrossAzimuth);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fireLocation, 10));
        showConversionsFragment(fireLocation);


    }

    /**
     * Uses VolleyRequester to request elevations for different lat lngs from lookout along azimuth
     * Has to break this information into multiple requests.
     */
    private void noCrossVisibleBase(){
        lookoutLocation = new Location("");
        lookoutLocation.setLatitude(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0));
        lookoutLocation.setLongitude(prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0));

        LatLng startLocation = new LatLng(lookoutLocation.getLatitude(), lookoutLocation.getLongitude());
        volleyRequester = new VolleyRequester();
        Arrays.fill(retrievalArray, Boolean.FALSE);
        int distAddition = (Constants.MAX_ELEVATOIN_REQUEST_DISTANCE - Constants.START_ELEVATION_REQUEST_DISTANCE)/Constants.NUM_ELEVATION_REQUESTS;
        for(int i = 0; i<Constants.NUM_ELEVATION_REQUESTS; i++){
            resultArrays.add(null);
            LatLng point1 = calculateLinePoint(i*distAddition + Constants.START_ELEVATION_REQUEST_DISTANCE, horizontalAzimuth, startLocation);
            LatLng point2 = calculateLinePoint((i+1)*distAddition + Constants.START_ELEVATION_REQUEST_DISTANCE , horizontalAzimuth, startLocation);
            volleyRequester.requestMultipleElevations(point1.latitude, point1.longitude, point2.latitude,
                    point2.longitude, getContext(), this, Constants.NUM_ELEVATIONS_PER_REQUEST, i);
        }
    }

    /**
     * Called when VolleyRequester gets results from Google Elevation API.
     * We are sending multiple requests, so this function stores the results. If this is
     * the last request sent, begins calculations. Otherwise, waits for other results
     * @param results
     * @param resultInt
     */
    public void elevationResult(JSONArray results, int resultInt){

        if(allRequestsComplete()){
            return;
        }

        resultArrays.set(resultInt, results);
        retrievalArray[resultInt] = true;
        if(allRequestsComplete()){
            calculateBestMatch();
        }

    }

    private LocationElevation parseJsonLocationElevation(JSONObject object) throws JSONException {
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

    private String getSnippet(double elevationDiff){
        String descriptor = getString(R.string.higher);
        if(elevationDiff < 0){
            descriptor = getString(R.string.lower);
        }

        String snippet =  getString(R.string.this_point_is) + " " + (String.format( "%.2f", Math.abs(elevationDiff))) + " " + getString(R.string.meters) +
                "(" + String.format( "%.2f", meterToFoot(Math.abs(elevationDiff))) + " " + getString(R.string.feet)+ ")\n"
                + descriptor + " " + getString(R.string.than_it_should_be);
        return snippet;
    }

    public void singleElevationRetrieved(JSONArray results){
        if(results != null && curClickMarker != null && results.length() > 0){

            try {
                JSONObject elevationObj = results.getJSONObject(0);
                LocationElevation clickedLocationElevation = parseJsonLocationElevation(elevationObj);

                //in meters
                double len = lookoutLocation.distanceTo(clickedLocationElevation.location);
                double radiansOnLine = Math.toRadians(verticalAzimuth);
                double tanOnLine = Math.tan(radiansOnLine);
                double heightDiff = len * tanOnLine;

                //elevation in feet, convert to meters
                double lookoutElevation = prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, 0);
                lookoutElevation = footToMeter(lookoutElevation);

                double targetElevation = lookoutElevation + heightDiff;

                //Gets difference between target elevation and actual elevation
                double elevationDiff = clickedLocationElevation.elevation - targetElevation;



                if(curClickMarker != null) {
                    curClickMarker.remove();
                }

                LatLng markerLocation = new LatLng(clickedLocationElevation.location.getLatitude(),
                        clickedLocationElevation.location.getLongitude());
                curClickMarker = createMarker(getString(R.string.clicked_location),
                        getSnippet(elevationDiff), BitmapDescriptorFactory.HUE_VIOLET, markerLocation);
                curClickMarker.showInfoWindow();
                drawLine(new LatLng(lookoutLocation.getLatitude(), lookoutLocation.getLongitude()), markerLocation);


            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    }


    public static double footToMeter(double foot){
        return foot * 0.3048;
    }
    public static double meterToFoot(double meter){
        return meter / 0.3048;
    }

    /**
     * Checks if all the elevation requests are completed
     * @return
     */
    private boolean allRequestsComplete(){
        for(int i = 0; i<retrievalArray.length; i++){
            if(retrievalArray[i] == false){
                return false;
            }
        }
        return true;
    }

    /**
     * Combines two arrays from elevation requests
     * a single large array of JSONs
     * @param arr1
     * @param arr2
     * @return
     */
    //TODO: some were getting negative volley responses. Figure out why.
    private ArrayList<LocationElevation> concatArray(ArrayList<LocationElevation> arr1, JSONArray arr2){
        ArrayList<LocationElevation> result = new ArrayList<LocationElevation>();
        if(arr2 == null){
            return arr1;
        }
        for (int i = 0; i < arr1.size(); i++) {
            result.add(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            try {
                JSONObject curObj = (JSONObject) arr2.get(i);
                LocationElevation obj = parseJsonLocationElevation(curObj);
                result.add(obj);
            } catch (JSONException e) {
                Log.e("ERROR", "JSONException" + e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Combines all arrays from elevation requests into a single large array called elevationArr
     */
    private void concatAllArrays(){
        elevationArr = new ArrayList<LocationElevation>();
        for(int i = 0; i<retrievalArray.length; i++){
            if(resultArrays.get(i) != null) {
                elevationArr = concatArray(elevationArr, resultArrays.get(i));
            }
        }
    }

    /**
     * Calculates the best location matches using the elevation data retrieved from Google Elevation
     * API. There are three possibilities for calculating the best location. To explain these options,
     * I will describe the estimated fire elevation calculated using the lookout location, azimuth, vertical angle, and
     * distance from lookout as the target elevation. I will describe the actual elevation of that point
     * as retrieved from Google's Elevation API as the real elevation.
     *
     * 1) When cycling through the array of real elevations, where each index i is closer to the lookout than i+1,
     * if array[i] is below the target elevation, and array[i+1] is above the target elevation, then the fire is between
     * these two points.
     *
     * 2) If the fire is located on top of a ridge, the line drawn through 3D space from the lookout based on the azimuth
     * and vertical angle may not intersect with earth's surface. In this case, an estimated degree of error is considered.
     * If the point is within this degree of error, it is a possible fire location.
     *
     * 3) If neither of these options is fulfilled, finds the point where the target elevation is closest to the actual elevation
     *
     *
     * @return ArrayList containing the possible locations arranged from most likely to least likely
     */
    //TODO: make sure closest location is visible... Location could be hidden by a ridge
    private ArrayList<LocationElevation> calculateBestMatch(){
        concatAllArrays();

        matches.clear();

        //Starts at lookout
        Location startLocation = new Location("");
        startLocation.setLatitude(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0));
        startLocation.setLongitude(prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0));

        //Picks NUM_OPTIONS_TO_DISPLAY possible points, starting with the best
        for(int l = 0; l<Constants.NUM_OPTIONS_TO_DISPLAY; l++) {

            LocationElevation closestPoint = null;

            boolean lastLocationBelowElevation = true;

            double curClosestElevationDiff = -1;

            for (int i = 0; i < elevationArr.size(); i++) {

                //Parses the JSONObject to get lat, lng, and elevation of point

                   LocationElevation curLocationToCheck = elevationArr.get(i);

                    //in meters
                    double len = startLocation.distanceTo(curLocationToCheck.location);
                    double radiansOnLine = Math.toRadians(verticalAzimuth);
                    double tanOnLine = Math.tan(radiansOnLine);
                    double radiansDegreeOfError = Math.toRadians(verticalAzimuth + Constants.VERTICAL_DEGREE_OF_ERROR);
                    double tanDegreeOfError = Math.tan(radiansDegreeOfError);
                    double heightDiff = len * tanOnLine;
                    double errorHeightDiff = len * tanDegreeOfError;
                    double maxAcceptableErrorDiff = Math.abs(heightDiff - errorHeightDiff);

                    //int feet, change to meters
                    double lookoutElevation = prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, 0);
                    lookoutElevation = footToMeter(lookoutElevation);

                    double targetElevation = lookoutElevation + heightDiff;

                    //Gets difference between target elevation and actual elevation
                    double elevationDiffAbs = Math.abs(targetElevation - curLocationToCheck.elevation);
                    double elevationDiff = curLocationToCheck.elevation - targetElevation;

                    //Checks if intersection happened between elevationArr[i-1] and elevationArr[i]
                    //if so, adds as a possible match and breaks to begin looking for next best match
                    if (i > 0) {
                        if (elevationDiff > 0 && lastLocationBelowElevation) {
                            if (checkIfItemMatch(curLocationToCheck.location, matches)) {
                                closestPoint = curLocationToCheck;
                                curClosestElevationDiff = elevationDiff;
                                break;
                            }
                        }
                    }

                    //Indicates whether the last location was below or above location so we can check
                    //for future intersects
                    if (elevationDiff > 0) {
                        lastLocationBelowElevation = true;
                    } else {
                        lastLocationBelowElevation = false;
                    }

                    //If the elevation difference is within the acceptable degree of error,
                    //adds a match and breaks to continue looking for next best match
                    if (elevationDiffAbs < maxAcceptableErrorDiff) {
                        if (checkIfItemMatch(curLocationToCheck.location, matches)) {
                            closestPoint = curLocationToCheck;
                            curClosestElevationDiff = elevationDiff;
                            break;
                        }
                    }

                    //If no point is in acceptable error degree, picks closest point
                    if(matches.size() == 0) {
                        if (curClosestElevationDiff == -1) {
                            if (checkIfItemMatch(curLocationToCheck.location, matches)) {
                                closestPoint = curLocationToCheck;
                                curClosestElevationDiff = elevationDiff;
                            }
                        } else if (elevationDiffAbs < Math.abs(curClosestElevationDiff)) {
                            if (checkIfItemMatch(curLocationToCheck.location, matches)) {
                                closestPoint = curLocationToCheck;
                                curClosestElevationDiff = elevationDiff;
                            }
                        }
                    }
            }

            if(closestPoint != null) {
                matches.add(new LocationElevation(closestPoint.location, curClosestElevationDiff));
            }
        }

        drawMarkersAndLines(matches, startLocation);
        return matches;

    }

    /**
     * Checks if Location or nearby location is already included in matches.
     * @param curLocationToCheck
     * @param matches
     * @return false if curLocationToCheck is already in matches, or within a certain distance of an item in matches. True otherwise
     */
    private boolean checkIfItemMatch(Location curLocationToCheck, ArrayList<LocationElevation> matches){
        boolean addItem = true;
        for (int k = 0; k < matches.size(); k++) {
            if (curLocationToCheck.distanceTo(matches.get(k).location) <= Constants.DISTANCE_BETWEEN_VISIBLE_POINTS) {
                addItem = false;
                break;
            }
        }
        return addItem;

    }

    /**
     * Draws markers at matches and the line representing horizontal azimuth from lookout
     * @param locations where best guess is at index 0, and guesses get worse at higher indices
     * @param startLocation
     */
    private void drawMarkersAndLines(ArrayList<LocationElevation> locations, Location startLocation){
        for(int i = 0; i<locations.size(); i++) {

            LatLng latLng = new LatLng(locations.get(i).location.getLatitude(), locations.get(i).location.getLongitude());
            double elevationDiff = locations.get(i).elevation;

            //Calculates marker color based on how good of an option it is
            int multiplicand = 100/(Constants.NUM_OPTIONS_TO_DISPLAY);
            int r = (i+1) * multiplicand;
            int g = 100 - r;
            float[] hsv = new float[3];
            Color.RGBToHSV(r, g, 20, hsv);

            createMarker(getString(R.string.guess_num) + (i+1), getSnippet(elevationDiff), hsv[0], latLng);
            drawLine(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()), latLng);


            if(i == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                showConversionsFragment(latLng);
            }
        }
    }

    private void drawLine(LatLng start, LatLng end){
        //Draws line from lookout to estimated points
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(start);
        polylineOptions.add(end);
        polylineOptions.width(4);
        polylineOptions.geodesic(true);
        polylineOptions.color(Color.BLACK);
        mMap.addPolyline(polylineOptions);
    }


    private Marker createMarker(String title, String snippet, float hue, LatLng position){
        MarkerOptions marker = new MarkerOptions();
        marker.position(position);

        //sets marker title and snippet
        marker.title(title);
        marker.snippet(snippet);

        //crates marker icon using color calculated above
        marker.icon(BitmapDescriptorFactory.defaultMarker(hue));
        return mMap.addMarker(marker);
    }

    private void getElevationOfPoint(LatLng point){
        if(volleyRequester == null){
            volleyRequester = new VolleyRequester();
        }
        volleyRequester.requestSingleElevation(point.latitude, point.longitude, this, getContext());
    }


    /**
     * Figures out legal based on lat lng of clicked marker, displays this info in a ShowConversionsFragment
     * @param conversionPoint
     */
    private void showConversionsFragment(LatLng conversionPoint){

        GeoConverter geoConverter = new GeoConverter();
        FragmentManager fm = getChildFragmentManager();

        showConversionsFragment = (ShowConversionsFragment) fm.findFragmentById(0);
        if(showConversionsFragment == null){
            showConversionsFragment = showConversionsFragment.newInstance();

        }
        fm.beginTransaction().replace(R.id.conversions_container, showConversionsFragment).commit();


        geoConverter.requestLegalFromLocation(showConversionsFragment,
                Location.convert(String.valueOf(conversionPoint.latitude)),
                Location.convert(String.valueOf(conversionPoint.longitude)));
    }

    /**
     * Shows a ShowConversionFragment when there is no conversion to show yet
     */
    private void showConversionsFragment(){
        FragmentManager fm = getChildFragmentManager();
        showConversionsFragment = (ShowConversionsFragment) fm.findFragmentById(0);
        if(showConversionsFragment == null){
            showConversionsFragment = showConversionsFragment.newInstance();

        }
        fm.beginTransaction().replace(R.id.conversions_container, showConversionsFragment).commit();
    }

    /**
     * calculates the LatLng of a point that lies a certain distance and
     * direction from the reporter's location
     *
     * @param distance
     * @param azimuth
     * @return LatLng point
     */
    private LatLng calculateLinePoint(float distance, float azimuth, LatLng latLng){
        LatLng point = com.google.maps.android.SphericalUtil.computeOffset(latLng, distance, azimuth);
        return point;
    }


    /**
     * Checks if location is enabled
     * @return
     */
    private boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    /**
     * Class to represent the location and elevation of a point
     */
    class LocationElevation {
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


}
