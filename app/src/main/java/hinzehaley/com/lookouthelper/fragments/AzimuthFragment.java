package hinzehaley.com.lookouthelper.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AzimuthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AzimuthFragment extends Fragment {

    private CheckBox checkboxBaseVisible;
    private CheckBox checkboxHaveCross;
    private LinearLayout layoutCross;
    private LinearLayout layoutVerticalAzimuth;
    private EditText etHorizontalAzimuthDegrees;
    private EditText etHorizontalAzimuthMintues;
    private EditText etVerticalAzimuthDegrees;
    private EditText etVerticalAzimuthMinutes;
    private EditText etSmokeColor;
    private EditText etLandmark;
    private EditText etAdditionalInfo;
    private Spinner spinnerCrossLookout;
    private EditText etCrossHorizontalAzimuthDegrees;
    private EditText etCrossHorizontalAzimuthMinutes;
    private Button btnNext;
    private View v;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment AzimuthFragment.
     */
    public static AzimuthFragment newInstance() {
        AzimuthFragment fragment = new AzimuthFragment();
        return fragment;
    }

    public AzimuthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_azimuth, container, false);
        getViewReferences();
        setCheckboxListeners();
        populateSpinner();
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HomeScreen screen = (HomeScreen)getActivity();
                if(screen.isConnectedToNetwork()) {
                    goToMapFragment();
                }else{
                    screen.showBasicErrorMessage(getString(R.string.no_internet));
                }
            }
        });

        return v;
    }

    private void getViewReferences(){
        checkboxBaseVisible = (CheckBox) v.findViewById(R.id.checkbox_base_visible);
        checkboxHaveCross = (CheckBox) v.findViewById(R.id.checkbox_have_cross);
        layoutCross = (LinearLayout) v.findViewById(R.id.layout_cross);
        layoutVerticalAzimuth = (LinearLayout) v.findViewById(R.id.layout_vertical_azimuth);
        etHorizontalAzimuthDegrees = (EditText) v.findViewById(R.id.et_horizontal_azimuth_degrees);
        etHorizontalAzimuthMintues = (EditText) v.findViewById(R.id.et_horizontal_azimuth_minutes);
        etVerticalAzimuthDegrees = (EditText) v.findViewById(R.id.et_vertical_azimuth_degrees);
        etVerticalAzimuthMinutes = (EditText) v.findViewById(R.id.et_vertical_azimuth_minutes);
        etSmokeColor = (EditText) v.findViewById(R.id.et_smoke_color);
        etLandmark = (EditText) v.findViewById(R.id.et_landmark);
        etAdditionalInfo = (EditText) v.findViewById(R.id.et_additional_info);
        spinnerCrossLookout = (Spinner) v.findViewById(R.id.spinner_cross_lookout);
        etCrossHorizontalAzimuthDegrees = (EditText) v.findViewById(R.id.et_horizontal_azimuth_cross_degrees);
        etCrossHorizontalAzimuthMinutes = (EditText) v.findViewById(R.id.et_horizontal_azimuth_cross_minutes);
        btnNext = (Button) v.findViewById(R.id.btn_next);
    }

    private void setCheckboxListeners(){
        checkboxHaveCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkboxHaveCross.isChecked()){
                    layoutCross.setVisibility(View.VISIBLE);
                    layoutVerticalAzimuth.setVisibility(View.GONE);
                }else{
                    layoutCross.setVisibility(View.GONE);
                    layoutVerticalAzimuth.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void populateSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }

        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        ArrayList<String> lookoutNames = new ArrayList();
        boolean gettingLookouts = true;
        int lookoutNumber = 0;
        while(gettingLookouts){
            String lookoutKey = Constants.CROSS_LOOKOUT_PREFERENCES_KEY + lookoutNumber;
            String lookoutName = prefs.getString(lookoutKey, null);
            if(lookoutName != null){
                lookoutNames.add(lookoutName);
                adapter.add(lookoutName);
            }else{
                gettingLookouts = false;
            }
            lookoutNumber += 1;
        }

        adapter.add("--Cross Lookout--");

        spinnerCrossLookout.setAdapter(adapter);
        spinnerCrossLookout.setSelection(adapter.getCount()); //display hint


    }

    private void goToMapFragment(){

        hideSoftKeyboard(getActivity());

        Float horizontalAzimuth = convertDegreesMinutesToFloat(getIntFromEt(etHorizontalAzimuthDegrees), getIntFromEt(etHorizontalAzimuthMintues));
        Float verticalAzimuth = convertDegreesMinutesToFloat(getIntFromEt(etVerticalAzimuthDegrees), getIntFromEt(etVerticalAzimuthMinutes));
        Float crossHorizontalAzimuth = convertDegreesMinutesToFloat(getIntFromEt(etCrossHorizontalAzimuthDegrees), getIntFromEt(etCrossHorizontalAzimuthMinutes));


        Log.i("BUG", "going to map fragment with vert azimuth : " + verticalAzimuth);


        MapReportFragment mapReportFragment = MapReportFragment.newInstance(checkboxBaseVisible.isChecked(), checkboxHaveCross.isChecked(),
                horizontalAzimuth, verticalAzimuth, spinnerCrossLookout.getSelectedItem().toString(), crossHorizontalAzimuth,
                etSmokeColor.getText().toString(), etLandmark.getText().toString(), etAdditionalInfo.getText().toString());

        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.goToMapFragment(mapReportFragment);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private int getIntFromEt(EditText et){
        if(containsInt(et)) {
            Log.i("BUG", "contains int! et text is : " + et.getText().toString());
            return Integer.parseInt(et.getText().toString());
        }else{
            Log.i("BUG", "DOES NOT contain int! et text is : " + et.getText().toString());
            return 0;
        }
    }

    private Float convertDegreesMinutesToFloat(int degrees, int minutes){

        if(degrees < 0 && minutes >= 0){
            minutes = -minutes;
        }

        Log.i("BUG", "degrees : " + degrees + ", minutes: " + minutes);


        float decimalDegrees = (float) (minutes/60.0);
        return degrees + decimalDegrees;
    }


    private boolean containsInt(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches("[0-9,-]*")){
                return true;
            }
        }
        return false;
    }

    private boolean containsText(EditText et){
        if(et.getText() != null){
            if(!et.getText().toString().equals("")){
                return true;
            }
        }
        return false;
    }




}
