package hinzehaley.com.lookouthelper.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.Interfaces.SingleElevationListener;
import hinzehaley.com.lookouthelper.PreferencesKeys;
import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.models.JSONParser;
import hinzehaley.com.lookouthelper.models.LocationElevation;
import hinzehaley.com.lookouthelper.models.VolleyRequester;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Fragment for inputting information about a smoke
 */
public class InfoReportFragment extends Fragment implements SingleElevationListener{

    private CheckBox checkboxBaseVisible;
    private CheckBox checkboxHaveCross;
    private LinearLayout layoutCross;
    private LinearLayout layoutVerticalAzimuth;
    private EditText etHorizontalAzimuthDegrees;
    private EditText etHorizontalAzimuthMintues;
    private EditText etVerticalAzimuthDegrees;
    private EditText etVerticalAzimuthMinutes;
    private Spinner spinnerCrossLookout;
    private EditText etCrossHorizontalAzimuthDegrees;
    private EditText etCrossHorizontalAzimuthMinutes;
    private Button btnNext;
    private View v;
    private LocationElevation yourLocationElevation;
    private SharedPreferences prefs;

    private LinearLayout mainLayout;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment InfoReportFragment.
     */
    public static InfoReportFragment newInstance() {
        InfoReportFragment fragment = new InfoReportFragment();
        return fragment;
    }

    public InfoReportFragment() {
        // Required empty public constructor
    }

    /**
     * Gets references to view items, listens for checkbox clicks to change view if
     * necessary, populates spinners
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_azimuth, container, false);
        // gets lookout names from SharedPreferences and adds them to adapter
        prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);

        HomeScreen homeScreen = (HomeScreen) getActivity();
        requestSingleLocationElevation();
        getViewReferences();
        setCheckboxListeners();
        populateSpinner();

        //Checks if connected to network. If not, shows error message. Otherwise proceeds
        //to the map fragment
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HomeScreen screen = (HomeScreen)getActivity();
                if(screen.isConnectedToNetwork()) {
                    if(haveNecessaryItems()) {
                        goToMapFragment();
                    }
                }else{
                    screen.showBasicErrorMessage(getString(R.string.no_internet));
                }

            }
        });

        //Hides soft keyboard when user clicks off of an EditText
        mainLayout = (LinearLayout) v.findViewById(R.id.layout_main_azimuth);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                return true;
            }
        });


        //Makes back button visible in toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        return v;
    }

    private void requestSingleLocationElevation(){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        if(homeScreen.isUsingLocation) {
            if(homeScreen.isConnectedToNetwork()) {
                VolleyRequester requester = VolleyRequester.getInstance();
                requester.requestSingleElevation(homeScreen.mLastLocation.getLatitude(), homeScreen.mLastLocation.getLongitude(), this, getContext());
            }else{
                homeScreen.showErrorDialog(getString(R.string.no_internet));
            }
        }
    }

    private boolean haveNecessaryItems(){
        HomeScreen mainActivity = (HomeScreen) getActivity();

        if (checkboxHaveCross.isChecked()){
            if(spinnerCrossLookout.getSelectedItem() == null || spinnerCrossLookout.getSelectedItem().toString().equals(getString(R.string.cross_lookout_prompt))){
                mainActivity.showBasicErrorMessage(getString(R.string.no_cross_selected));
                return false;
            }
        }
        if(yourLocationElevation == null && mainActivity.isUsingLocation){
            mainActivity.showBasicErrorMessage(getString(R.string.unable_to_get_your_elevation));
            return false;
        }
        return true;
    }

    @Override
    public void singleElevationRetrieved(JSONArray arr){
        if(arr != null) {
            try {
                yourLocationElevation = JSONParser.parseSingleElevation(arr);
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.showErrorDialog(getString(R.string.unable_to_get_your_elevation));
        homeScreen.goToHomeFragment();
    }

    /**
     * gets references to view items
     */
    private void getViewReferences(){
        checkboxBaseVisible = (CheckBox) v.findViewById(R.id.checkbox_base_visible);
        checkboxHaveCross = (CheckBox) v.findViewById(R.id.checkbox_have_cross);
        layoutCross = (LinearLayout) v.findViewById(R.id.layout_cross);
        layoutVerticalAzimuth = (LinearLayout) v.findViewById(R.id.layout_vertical_azimuth);
        etHorizontalAzimuthDegrees = (EditText) v.findViewById(R.id.et_horizontal_azimuth_degrees);
        etHorizontalAzimuthMintues = (EditText) v.findViewById(R.id.et_horizontal_azimuth_minutes);
        etVerticalAzimuthDegrees = (EditText) v.findViewById(R.id.et_vertical_azimuth_degrees);
        etVerticalAzimuthMinutes = (EditText) v.findViewById(R.id.et_vertical_azimuth_minutes);
        spinnerCrossLookout = (Spinner) v.findViewById(R.id.spinner_cross_lookout);
        etCrossHorizontalAzimuthDegrees = (EditText) v.findViewById(R.id.et_horizontal_azimuth_cross_degrees);
        etCrossHorizontalAzimuthMinutes = (EditText) v.findViewById(R.id.et_horizontal_azimuth_cross_minutes);
        btnNext = (Button) v.findViewById(R.id.btn_next);
    }

    /**
     * Listens for checkbox clicks and alters view as necessary when
     * checkbox is selected or de-selected
     */
    private void setCheckboxListeners(){
        checkboxHaveCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkboxHaveCross.isChecked()){
                    showCrossClicked();
                }else{
                    layoutCross.setVisibility(View.GONE);
                    layoutVerticalAzimuth.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showCrossClicked(){
        layoutCross.setVisibility(View.VISIBLE);
        layoutVerticalAzimuth.setVisibility(View.GONE);
        if(spinnerCrossLookout.getAdapter().getCount() == 0) {
            HomeScreen mainActivity = (HomeScreen) getActivity();
            mainActivity.showBasicErrorMessage(getString(R.string.no_cross_lookouts));
        }
    }

    /**
     * Populates spinner with the names of all cross lookouts stored in SharedPreferences
     */
    private void populateSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount()));
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // Last item used as hint
            }

        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



        ArrayList<String> lookoutNames = new ArrayList();
        boolean gettingLookouts = true;
        int lookoutNumber = 0;
        while(gettingLookouts){
            String lookoutKey = PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + lookoutNumber;
            String lookoutName = prefs.getString(lookoutKey, null);
            if(lookoutName != null){
                lookoutNames.add(lookoutName);
                adapter.add(lookoutName);
            }else{
                gettingLookouts = false;
            }
            lookoutNumber += 1;
        }

        adapter.add(getString(R.string.cross_lookout_prompt));

        spinnerCrossLookout.setAdapter(adapter);
        spinnerCrossLookout.setSelection(adapter.getCount()); //display hint


    }

    private LatLng getCrossLocation(String name){
        float lat = prefs.getFloat(name + PreferencesKeys.LAT, 100000);
        float lng = prefs.getFloat(name + PreferencesKeys.LON, 100000);
        if(lat != 100000 && lng != 100000) {
            return new LatLng(lat, lng);
        }
        return null;
    }

    private LocationElevation getLocationElevation(){
        float lat = prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 100000);
        float lng = prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 100000);
        float elevation = prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, -100000);
        if(lat != 100000 && lng != 100000 && elevation != -100000) {
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);
            return new LocationElevation(location, footToMeter(elevation));
        }
        return null;
    }

    public static double footToMeter(double foot){
        return foot * 0.3048;
    }

    /**
     * Proceeds to map fragment
     */
    private void goToMapFragment(){

        hideSoftKeyboard(getActivity());

        Float horizontalAzimuth = convertDegreesMinutesToFloat(getIntFromEt(etHorizontalAzimuthDegrees), getIntFromEt(etHorizontalAzimuthMintues));
        Float verticalAzimuth = convertDegreesMinutesToFloat(getIntFromEt(etVerticalAzimuthDegrees), getIntFromEt(etVerticalAzimuthMinutes));
        Float crossHorizontalAzimuth = convertDegreesMinutesToFloat(getIntFromEt(etCrossHorizontalAzimuthDegrees), getIntFromEt(etCrossHorizontalAzimuthMinutes));

        String spinnerItem = "";
        double crossLat = 0;
        double crossLng = 0;

        HomeScreen homeScreen = (HomeScreen) getActivity();


        if(spinnerCrossLookout.getSelectedItem() != null){
            spinnerItem = spinnerCrossLookout.getSelectedItem().toString();
            LatLng crossLocation = getCrossLocation(spinnerItem);

            if(crossLocation == null){
                homeScreen.showBasicErrorMessage(getString(R.string.no_lookout_set));
                return;
            }
            crossLat = crossLocation.latitude;
            crossLng = crossLocation.longitude;
        }

        if(!setCorrectStartLocationElevation()){
            return;
        }

        MapReportFragment mapReportFragment = MapReportFragment.newInstance(checkboxBaseVisible.isChecked(), checkboxHaveCross.isChecked(),
                horizontalAzimuth, verticalAzimuth, crossHorizontalAzimuth, yourLocationElevation, crossLat, crossLng);

        homeScreen.goToMapFragment(mapReportFragment);
    }

    private boolean setCorrectStartLocationElevation(){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        if(homeScreen.isUsingLocation){
            if(yourLocationElevation == null){
                requestSingleLocationElevation();
                if(homeScreen.mLastLocation == null){
                    homeScreen.showBasicErrorMessage(getString(R.string.unable_to_get_location));
                    return false;
                }else{
                    requestSingleLocationElevation();
                    homeScreen.showBasicErrorMessage(getString(R.string.unable_to_get_your_elevation));
                    return false;
                }
            }
        }else{
            yourLocationElevation = getLocationElevation();
            if(yourLocationElevation == null){
                homeScreen.showBasicErrorMessage(getString(R.string.no_lookout_set));
                return false;
            }
        }
        return true;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     *
     * @param et
     * @return int if et contains int, 0 otherwise
     */
    private int getIntFromEt(EditText et){
        if(containsInt(et)) {
            return Integer.parseInt(et.getText().toString());
        }else{
            return 0;
        }
    }

    /**
     * Converts degrees and minutes from azimuth or vertical angle into a float representation
     * @param degrees
     * @param minutes
     * @return
     */
    private Float convertDegreesMinutesToFloat(int degrees, int minutes){

        if(degrees < 0 && minutes >= 0){
            minutes = -minutes;
        }

        float decimalDegrees = (float) (minutes/60.0);
        return degrees + decimalDegrees;
    }

    /**
     * @param et
     * @return true if et contains int, false otherwise
     */
    private boolean containsInt(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches("[0-9,-]*")){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param et
     * @return true if et contains some text, false if empty
     */
    private boolean containsText(EditText et){
        if(et.getText() != null){
            if(!et.getText().toString().equals("")){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkboxHaveCross.isChecked()){
            showCrossClicked();
        }
        populateSpinner();
    }


}
