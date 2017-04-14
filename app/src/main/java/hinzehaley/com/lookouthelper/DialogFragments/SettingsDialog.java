package hinzehaley.com.lookouthelper.DialogFragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsDialog extends DialogFragment {

    private View v;
    private Spinner spinnerState;
    private EditText etPrincipalMeridian;
    private EditText etLat;
    private EditText etLon;
    private EditText etElevation;
    private SharedPreferences prefs;
    private Button btnDone;
    private Button btnCancel;
    ArrayAdapter<CharSequence> adapterStates;

    public SettingsDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_settings_dialog, container, false);
        spinnerState = (Spinner) v.findViewById(R.id.spinner_state);
        etElevation = (EditText) v.findViewById(R.id.et_lookout_elevation);
        etLat = (EditText) v.findViewById(R.id.et_lookout_lat);
        etLon = (EditText) v.findViewById(R.id.et_lookout_lon);
        etPrincipalMeridian = (EditText) v.findViewById(R.id.et_meridian);
        btnDone = (Button) v.findViewById(R.id.btn_save_settings);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel_settings);
        fillInSpinners();
        setUpEditTexts();
        prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        fillInCompletedFields();

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        return v;
    }


    private void fillInCompletedFields(){
        if(prefs.getString(Constants.STATE_PREFERENCES_KEY, null) != null){
            spinnerState.setSelection(adapterStates.getPosition(prefs.getString(Constants.STATE_PREFERENCES_KEY, null)));
        }
        if(prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0) != 0){
            etLat.setText("" + prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0));
        }
        if(prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0) != 0){
            etLon.setText(""+prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0));
        }
        if(prefs.getFloat(Constants.LOOKOUT_ELEVATION_PREFERENCES_KEY, -1) != -1){
            etElevation.setText(""+prefs.getFloat(Constants.LOOKOUT_ELEVATION_PREFERENCES_KEY, 0));
        }
        if(prefs.getInt(Constants.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1) != -1){
            etPrincipalMeridian.setText(""+prefs.getInt(Constants.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, 0));
        }
    }

    //TODO: add else checks to see if valid....
    private void saveSettings(){
        String state = spinnerState.getSelectedItem().toString();
        Log.i("Saving preferences", "state is : " + state);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        if(state != null){
            if(!state.equals("")){
                prefsEditor.putString(Constants.STATE_PREFERENCES_KEY, state);
            }
        }
        if(containsLatLng(etLat)) {
            Float latitude = Float.parseFloat(etLat.getText().toString());
            prefsEditor.putFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, latitude);
        }
        if(containsLatLng(etLon)) {
            Float longitude = Float.parseFloat(etLon.getText().toString());
            prefsEditor.putFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, longitude);
        }
        if(containsDecimal(etElevation)) {
            Float elevation = Float.parseFloat(etElevation.getText().toString());
            prefsEditor.putFloat(Constants.LOOKOUT_ELEVATION_PREFERENCES_KEY, elevation);
        }
        if(containsInt(etPrincipalMeridian)) {
            int principalMeridian = Integer.parseInt(etPrincipalMeridian.getText().toString());
            prefsEditor.putInt(Constants.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, principalMeridian);
        }
        prefsEditor.commit();
    }

    private boolean containsLatLng(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches("[0-9.:-]*")){
                return true;
            }
        }
        return false;
    }

    private boolean containsDecimal(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches("[0-9.]*")){
                return true;
            }
        }
        return false;
    }

    private boolean containsInt(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches("[0-9]*")){
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


    private void fillInSpinners() {
        adapterStates = ArrayAdapter.createFromResource(getContext(),
                R.array.states, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterStates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerState.setAdapter(adapterStates);
    }

    private void setUpEditTexts(){
        addNextButtonToEditText(etElevation);
        addNextButtonToEditText(etLat);
        addNextButtonToEditText(etLon);
        addNextButtonToEditText(etPrincipalMeridian);
    }

    private void addNextButtonToEditText(EditText et){
        et.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    }




}
