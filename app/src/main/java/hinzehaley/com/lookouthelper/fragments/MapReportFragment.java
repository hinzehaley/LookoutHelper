package hinzehaley.com.lookouthelper.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.models.GeoConverter;
import hinzehaley.com.lookouthelper.models.VolleyRequester;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapReportFragment extends Fragment implements OnMapReadyCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SMOKE_COLOR = "smokeColor";
    private static final String CROSS_LOOKOUT = "crossLookout";
    private static final String LANDMARK = "landmark";
    private static final String ADDITIONAL_INFO = "additionalInfo";
    private static final String BASE_VISIBLE = "baseVisible";
    private static final String HAVE_CROSS = "haveCross";
    private static final String HORIZONTAL_AZIMUTH = "horizontalAzimuth";
    private static final String VERTICAL_AZIMUTH = "verticalAzimuth";
    private static final String CROSS_AZIMUTH = "crossAzimuth";


    boolean[] retrievalArray = new boolean[Constants.NUM_ELEVATION_REQUESTS];
    private ArrayList<JSONArray> resultArrays = new ArrayList<>(Constants.NUM_ELEVATION_REQUESTS);

    private JSONArray elevationArr;

    // TODO: Rename and change types of parameters
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
    private View mapContainer;
    private View conversionsContainer;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    ShowConversionsFragment showConversionsFragment;
    private SharedPreferences prefs;


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_map_report, container, false);
        mapContainer = v.findViewById(R.id.map_container);

        prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);


        FragmentManager fm = getChildFragmentManager();
        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mMap == null) {
            mMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mMapFragment).commit();
        }
        showConversionsFragment();

        initilizeMap();


        return v;
    }


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

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if(checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        }
        map.getUiSettings().setZoomControlsEnabled(true);
        calculatePossibleFireLocation();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng conversionPoint = marker.getPosition();
                showConversionsFragment(conversionPoint);

                return false;
            }
        });
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

    }


    private void calculatePossibleFireLocation(){

        if(!haveCross && baseVisible){
            noCrossVisibleBase();
        }else if(haveCross){
            haveCross();
        }
    }


    private void haveCross(){
        Log.i("CROSS BUG", "have cross method");
        LatLng yourLocation = new LatLng(prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0), prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0));
        LatLng fireLocation = null;
        float crossLat = prefs.getFloat(crossLookout + "lat", 0);
        float crossLon = prefs.getFloat(crossLookout + "lon", 0);

        Log.i("CROSS BUG", "cross lat: " + crossLat + "cross lng: " + crossLon);


        LatLng crossLocation = new LatLng(crossLat, crossLon);
        double azimuthToCross = SphericalUtil.computeHeading(yourLocation, crossLocation);
        double azimuthFromCross = SphericalUtil.computeHeading(crossLocation, yourLocation);

        if(azimuthToCross < 0){
            azimuthToCross = 360 + azimuthToCross;
        }if(azimuthFromCross < 0){
            azimuthFromCross = 360 + azimuthFromCross;
        }

        Log.i("CROSS BUG", "azimuth to cross: " + azimuthToCross + "azimuth from cross: " + azimuthFromCross);



        float angleYou;


        if(horizontalAzimuth < azimuthToCross){
            angleYou = (float) (azimuthToCross - horizontalAzimuth);
        }else{
            angleYou = (float) (horizontalAzimuth - azimuthToCross);
        }

        float angleCross;

        if(crossAzimuth > azimuthFromCross){
            angleCross = (float) (crossAzimuth - azimuthFromCross);
        }else{
            angleCross = (float) (azimuthFromCross - crossAzimuth);
        }

        Log.i("CROSS BUG", "angle you: " + angleYou + "angle cross: " + angleCross);


        float angleFire = 180 - angleYou - angleCross;

        Log.i("CROSS BUG", "angle fire: " + angleFire);

        double distanceToCross = SphericalUtil.computeDistanceBetween(yourLocation, crossLocation);

        Log.i("CROSS BUG", "distance to cross: " + distanceToCross);


        double distanceFromYouToFire = Math.abs((distanceToCross * Math.sin(Math.toRadians(angleCross)))/Math.sin(Math.toRadians(angleFire)));
        Log.i("CROSS BUG", "distance to fire: " + distanceFromYouToFire);


        fireLocation = SphericalUtil.computeOffset(yourLocation, distanceFromYouToFire, horizontalAzimuth);

        MarkerOptions closestPointMarker = new MarkerOptions();
        closestPointMarker.position(fireLocation);
        closestPointMarker.title("FIRE LOCATION!");

        LatLng endOfYourAzimuth = SphericalUtil.computeOffset(yourLocation, Constants.MAX_ELEVATOIN_REQUEST_DISTANCE, horizontalAzimuth);
        LatLng endOfCrossAzimuth = SphericalUtil.computeOffset(crossLocation, Constants.MAX_ELEVATOIN_REQUEST_DISTANCE, crossAzimuth);


        mMap.addMarker(closestPointMarker);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(yourLocation);
        polylineOptions.add(endOfYourAzimuth);
        polylineOptions.width(4);
        polylineOptions.color(Color.BLUE);


        mMap.addPolyline(polylineOptions);

        PolylineOptions polylineOptions2 = new PolylineOptions();
        polylineOptions2.add(crossLocation);
        polylineOptions2.add(endOfCrossAzimuth);
        polylineOptions2.width(4);
        polylineOptions2.color(Color.BLUE);


        mMap.addPolyline(polylineOptions2);


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fireLocation, 10));
        showConversionsFragment(fireLocation);


    }

    private void noCrossVisibleBase(){
        LatLng startLocation = new LatLng(prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0), prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0));
        boolean closestPointRetrieved = false;
        LatLng closestPoint = startLocation;
        VolleyRequester volleyRequester = new VolleyRequester();

        Arrays.fill(retrievalArray, Boolean.FALSE);


        int distAddition = (Constants.MAX_ELEVATOIN_REQUEST_DISTANCE - Constants.START_ELEVATION_REQUEST_DISTANCE)/Constants.NUM_ELEVATION_REQUESTS;

        for(int i = 0; i<Constants.NUM_ELEVATION_REQUESTS; i++){
            resultArrays.add(null);
            LatLng point1 = calculateLinePoint(i*distAddition + Constants.START_ELEVATION_REQUEST_DISTANCE, horizontalAzimuth, startLocation);
            LatLng point2 = calculateLinePoint((i+1)*distAddition + Constants.START_ELEVATION_REQUEST_DISTANCE , horizontalAzimuth, startLocation);
            volleyRequester.requestElevation(point1.latitude, point1.longitude, point2.latitude,
                    point2.longitude, getContext(), this, Constants.NUM_ELEVATIONS_PER_REQUEST, i);
        }
    }

    public void elevationResult(JSONArray results, int resultInt){

        if(allRequestsComplete()){
            return;
        }

        //if(results != null) {
           // Log.i("NO CRASH", "results are not null for resultInt " + resultInt);

            resultArrays.set(resultInt, results);
            retrievalArray[resultInt] = true;

        //}else{
        //   Log.e("CRASH", "results are null for resultInt " + resultInt);
       // }


        if(allRequestsComplete()){
            calculateBestMatch();
        }

    }

    private boolean allRequestsComplete(){
        for(int i = 0; i<retrievalArray.length; i++){
            if(retrievalArray[i] == false){
                return false;
            }
        }
        return true;
    }

    //TODO: some were getting negative volley responses. Figure out why.
    private JSONArray concatArray(JSONArray arr1, JSONArray arr2){
        if(arr2 == null){
            return arr1;
        }
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            try {
                result.put(arr1.get(i));
            } catch (JSONException e) {
                Log.i("ERROR", "JSONException" + e.getMessage());
                e.printStackTrace();
            }
        }
        for (int i = 0; i < arr2.length(); i++) {
            try {
                result.put(arr2.get(i));
            } catch (JSONException e) {
                Log.i("ERROR", "JSONException" + e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    private void concatAllArrays(){
        elevationArr = new JSONArray();
        for(int i = 0; i<retrievalArray.length; i++){
            Log.i("CONCAT", " i = " + i);
            if(resultArrays.get(i) != null) {
                elevationArr = concatArray(elevationArr, resultArrays.get(i));
            }
        }
    }


    //TODO: make sure closest location is visible...
    private ArrayList<Location> calculateBestMatch(){
        Log.i("PAUSE", "calculating best match...");
        concatAllArrays();

        ArrayList<Location> matches = new ArrayList<Location>();
        ArrayList<Double> elevations = new ArrayList<Double>();
        Location startLocation = new Location("");
        startLocation.setLatitude(prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0));
        startLocation.setLongitude(prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0));

        for(int l = 0; l<Constants.NUM_OPTIONS_TO_DISPLAY; l++) {

            Location closestPoint = null;
            double closestElevation = -1;

            boolean lastLocationBelowElevation = true;

            double curClosestElevationDiff = -1;

            for (int i = 0; i < elevationArr.length(); i++) {

                try {
                    JSONObject curObj = (JSONObject) elevationArr.get(i);
                    double elevation = curObj.getDouble("elevation");
                    JSONObject location = curObj.getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lon = location.getDouble("lng");

                    Location curLocationToCheck = new Location("");
                    curLocationToCheck.setLatitude(lat);
                    curLocationToCheck.setLongitude(lon);


                    double len = startLocation.distanceTo(curLocationToCheck);

                    double radiansOnLine = Math.toRadians(verticalAzimuth);
                    double tanOnLine = Math.tan(radiansOnLine);

                    double radiansDegreeOfError = Math.toRadians(verticalAzimuth + Constants.VERTICAL_DEGREE_OF_ERROR);
                    double tanDegreeOfError = Math.tan(radiansDegreeOfError);


                    double heightDiff = len * tanOnLine;
                    double errorHeightDiff = len * tanDegreeOfError;

                    double maxAcceptableErrorDiff = Math.abs(heightDiff - errorHeightDiff);

                    double lookoutElevation = prefs.getFloat(Constants.LOOKOUT_ELEVATION_PREFERENCES_KEY, 0);
                    double shouldBeElevation = lookoutElevation + heightDiff;


                    double elevationDiffAbs = Math.abs(shouldBeElevation - elevation);

                    double elevationDiff = elevation - shouldBeElevation;

                    //Checks for if we missed ground hit.
                    if (i > 0) {
                        if (elevationDiff > 0 && lastLocationBelowElevation) {

                            if (checkIfItemMatch(curLocationToCheck, matches)) {
                                closestPoint = curLocationToCheck;
                                closestElevation = elevationDiffAbs;
                                curClosestElevationDiff = elevationDiff;
                                break;
                            }
                        }
                    }

                    if (elevationDiff > 0) {
                        lastLocationBelowElevation = true;
                    } else {
                        lastLocationBelowElevation = false;
                    }

                    if (elevationDiffAbs < maxAcceptableErrorDiff) {

                        if (checkIfItemMatch(curLocationToCheck, matches)) {
                            closestPoint = curLocationToCheck;
                            closestElevation = elevationDiffAbs;
                            curClosestElevationDiff = elevationDiff;
                            break;
                        }
                    }

                    //If no point is in acceptable error degree, picks closest point
                    if(matches.size() == 0) {
                        if (closestElevation == -1) {
                            if (checkIfItemMatch(curLocationToCheck, matches)) {
                                closestElevation = elevationDiffAbs;
                                closestPoint = curLocationToCheck;
                                curClosestElevationDiff = elevationDiff;
                            }
                        } else if (elevationDiffAbs < closestElevation) {
                            if (checkIfItemMatch(curLocationToCheck, matches)) {
                                closestElevation = elevationDiffAbs;
                                closestPoint = curLocationToCheck;
                                curClosestElevationDiff = elevationDiff;
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("ERROR", e.getMessage());
                }


            }

            if(closestPoint != null) {
                Log.i("POINT", "adding closest point " + l + " : " + closestPoint.getLatitude() + "  " + closestPoint.getLongitude());
                matches.add(closestPoint);
                elevations.add(curClosestElevationDiff);
            }
        }

        drawMarkersAndLines(matches, elevations, startLocation);
        return matches;

    }

    private boolean checkIfItemMatch(Location curLocationToCheck, ArrayList<Location> matches){
        boolean addItem = true;
        for (int k = 0; k < matches.size(); k++) {
            if (curLocationToCheck.distanceTo(matches.get(k)) <= Constants.DISTANCE_BETWEEN_VISIBLE_POINTS) {
                addItem = false;
                break;
            }
        }
        return addItem;

    }

    private void drawMarkersAndLines(ArrayList<Location> locations, ArrayList<Double> elevationDifferences, Location startLocation){
        for(int i = 0; i<locations.size(); i++) {

            LatLng latLng = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());

            double elevationDiff = elevationDifferences.get(i);


            //min = 20, max = 80
            //        first increase green, then decrease red
            int multiplicand = 100/(Constants.NUM_OPTIONS_TO_DISPLAY);
            int r = (i+1) * multiplicand;

            int g = 100 - r;

            float[] hsv = new float[3];
            Color.RGBToHSV(r, g, 20, hsv);
            Log.i("HUE", " hue is : " + hsv[0] +" for elevation: " + elevationDifferences.get(i) + " r is: " + r + " g is: " + g);

            String descriptor = " higher ";
            if(elevationDiff < 0){
                descriptor = " lower ";
            }
            String snippet = "This point is " + (String.format( "%.2f", Math.abs(elevationDiff))) + " meters" +
                    descriptor + "\nthan it should be based on the given \nvertical angle";


            MarkerOptions marker = new MarkerOptions();
            marker.position(latLng);
            marker.title("Guess # " + (i+1) );
            marker.snippet(snippet);

            marker.icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));

            mMap.addMarker(marker);

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.add(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
            polylineOptions.add(latLng);
            polylineOptions.width(4);
            polylineOptions.color(Color.BLUE);


            mMap.addPolyline(polylineOptions);

            if(i == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                showConversionsFragment(latLng);
            }
        }
    }


    /*//TODO: make sure closest location is visible...
    private void calculateSecondBestMatch(Location bestMatch){
        concatAllArrays();

        Location closestPoint = null;
        double closestElevation = -1;

        boolean lastLocationBelowElevation = true;

        Location startLocation = new Location("");
        startLocation.setLatitude(prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0));
        startLocation.setLongitude(prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0));

        for(int i = 0; i < elevationArr.length(); i++){

            try {
                JSONObject curObj = (JSONObject) elevationArr.get(i);
                double elevation = curObj.getDouble("elevation");
                JSONObject location = curObj.getJSONObject("location");
                double lat = location.getDouble("lat");
                double lon = location.getDouble("lng");

                Location curLocationToCheck = new Location("");
                curLocationToCheck.setLatitude(lat);
                curLocationToCheck.setLongitude(lon);



                double len = startLocation.distanceTo(curLocationToCheck);

                double radiansOnLine = Math.toRadians(verticalAzimuth);
                double tanOnLine = Math.tan(radiansOnLine);

                double radiansDegreeOfError = Math.toRadians(verticalAzimuth + Constants.VERTICAL_DEGREE_OF_ERROR);
                double tanDegreeOfError = Math.tan(radiansDegreeOfError);



                double heightDiff = len * tanOnLine;
                double errorHeightDiff = len * tanDegreeOfError;

                double maxAcceptableErrorDiff = Math.abs(heightDiff - errorHeightDiff);

                double lookoutElevation = prefs.getFloat(Constants.LOOKOUT_ELEVATION_PREFERENCES_KEY, 0);
                double shouldBeElevation = lookoutElevation + heightDiff;



                double elevationDiffAbs = Math.abs(shouldBeElevation - elevation);

                double elevationDiff = elevation - shouldBeElevation;

                //Checks for if we missed ground hit.
                if(i>0) {
                    if (elevationDiff > 0 && lastLocationBelowElevation) {
                        if(curLocationToCheck.distanceTo(bestMatch) > Constants.DISTANCE_BETWEEN_VISIBLE_POINTS) {
                            closestPoint = curLocationToCheck;
                        }
                        break;
                    }
                }

                if(elevationDiff > 0){
                    lastLocationBelowElevation = true;
                }else{
                    lastLocationBelowElevation = false;
                }

                if(elevationDiffAbs < maxAcceptableErrorDiff){
                    if(curLocationToCheck.distanceTo(bestMatch) > Constants.DISTANCE_BETWEEN_VISIBLE_POINTS) {
                        closestPoint = curLocationToCheck;
                    }
                    break;
                }

                //If no point is in acceptable error degree, picks closest point
                if(closestElevation == -1){
                    if(curLocationToCheck.distanceTo(bestMatch) > Constants.DISTANCE_BETWEEN_VISIBLE_POINTS) {
                        closestElevation = elevationDiffAbs;
                        closestPoint = curLocationToCheck;
                    }
                }

                else if(elevationDiffAbs < closestElevation){
                    if(curLocationToCheck.distanceTo(bestMatch) > Constants.DISTANCE_BETWEEN_VISIBLE_POINTS) {

                        closestElevation = elevationDiffAbs;
                        closestPoint = curLocationToCheck;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("ERROR", e.getMessage());
            }

        }

        LatLng closestPointLatLng = new LatLng(closestPoint.getLatitude(), closestPoint.getLongitude());
        MarkerOptions closestPointMarker = new MarkerOptions();
        closestPointMarker.position(closestPointLatLng);
        closestPointMarker.title("2nd BEST FIRE LOCATION!");


        Marker marker = mMap.addMarker(closestPointMarker);



        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()));
        polylineOptions.add(closestPointLatLng);
        polylineOptions.width(4);
        polylineOptions.color(Color.BLUE);


       mMap.addPolyline(polylineOptions);


       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(closestPointLatLng, 10));
       // showConversionsFragment(closestPointLatLng);

    }
*/


    private void showConversionsFragment(LatLng conversionPoint){
        conversionsContainer = v.findViewById(R.id.conversions_container);

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


    private boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }




}
