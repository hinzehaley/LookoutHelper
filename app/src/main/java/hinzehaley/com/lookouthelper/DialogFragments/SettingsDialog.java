package hinzehaley.com.lookouthelper.DialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.PreferencesKeys;
import hinzehaley.com.lookouthelper.R;

import static android.content.Context.INPUT_METHOD_SERVICE;

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
    LinearLayout mainLayout;

    public SettingsDialog() {
        // Required empty public constructor
    }


    /**
     * Gets view references and fills in fields that we have stored information
     * for.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        v = inflater.inflate(R.layout.fragment_settings_dialog, container, false);
        spinnerState = (Spinner) v.findViewById(R.id.spinner_state);
        etElevation = (EditText) v.findViewById(R.id.et_lookout_elevation);
        etLat = (EditText) v.findViewById(R.id.et_lookout_lat);
        etLon = (EditText) v.findViewById(R.id.et_lookout_lon);
        etPrincipalMeridian = (EditText) v.findViewById(R.id.et_meridian);
        btnDone = (Button) v.findViewById(R.id.btn_save_settings);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel_settings);
        fillInSpinners();
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

        mainLayout = (LinearLayout) v.findViewById(R.id.main_layout);

        //Hides soft keyboard if user clicks on the dialog -- so they can reach relevant buttons
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                return true;
            }
        });


        return v;
    }


    /**
     * Fills in fields that are already saved in SharedPreferences
     */
    private void fillInCompletedFields(){
        if(prefs.getString(PreferencesKeys.STATE_PREFERENCES_KEY, null) != null){
            spinnerState.setSelection(adapterStates.getPosition(prefs.getString(PreferencesKeys.STATE_PREFERENCES_KEY, null)));
        }
        if(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0) != 0){
            etLat.setText("" + prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0));
        }
        if(prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0) != 0){
            etLon.setText(""+prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0));
        }
        if(prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, -1) != -1){
            etElevation.setText(""+prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, 0));
        }
        if(prefs.getInt(PreferencesKeys.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1) != -1){
            etPrincipalMeridian.setText(""+prefs.getInt(PreferencesKeys.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, 0));
        }
    }



    /**
     * Saves information into SharedPreferences
     */
    //TODO: add else checks to see if valid input
    private void saveSettings(){
        String state = spinnerState.getSelectedItem().toString();
        Log.i("Saving preferences", "state is : " + state);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        if(state != null){
            if(!state.equals("")){
                prefsEditor.putString(PreferencesKeys.STATE_PREFERENCES_KEY, state);
            }
        }
        if(containsDecimal(etLat)) {
            Float latitude = Float.parseFloat(etLat.getText().toString());
            prefsEditor.putFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, latitude);
        }
        if(containsDecimal(etLon)) {
            Float longitude = Float.parseFloat(etLon.getText().toString());
            prefsEditor.putFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, longitude);
        }
        if(containsDecimal(etElevation)) {
            Float elevation = Float.parseFloat(etElevation.getText().toString());
            prefsEditor.putFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, elevation);
        }
        if(containsInt(etPrincipalMeridian)) {
            int principalMeridian = Integer.parseInt(etPrincipalMeridian.getText().toString());
            prefsEditor.putInt(PreferencesKeys.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, principalMeridian);
        }
        prefsEditor.commit();
    }

    /**
     * Ensures that et contains only numbers and a decimal
     * @param et
     * @return
     */
    private boolean containsDecimal(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches(Constants.DECIMAL_REGEX)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to make sure et contains only numbers
     * @param et
     * @return
     */
    private boolean containsInt(EditText et){
        if(containsText(et)){
            String text = et.getText().toString();
            if(text.matches("[0-9]*")){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to make sure et is not empty
     *
     * @param et
     * @return
     */
    private boolean containsText(EditText et){
        if(et.getText() != null){
            if(!et.getText().toString().equals("")){
                return true;
            }
        }
        return false;
    }


    /**
     * Fills in the spinner containing state names
     */
    private void fillInSpinners() {
        adapterStates = ArrayAdapter.createFromResource(getContext(),
                R.array.states, android.R.layout.simple_spinner_item);
        adapterStates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(adapterStates);
    }

    /**
     * Sets size of SettingsDialog and makes background transparent
     */
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
