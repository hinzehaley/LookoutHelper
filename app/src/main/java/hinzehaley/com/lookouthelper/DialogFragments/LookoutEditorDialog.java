package hinzehaley.com.lookouthelper.DialogFragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.Interfaces.OnAddCrossListener;
import hinzehaley.com.lookouthelper.PreferencesKeys;
import hinzehaley.com.lookouthelper.R;

/**
 * A DialogFragment for setting the name, elevation, and lat lon of a wildfire
 * lookout that may be used for crosses on wildfires
 */
public class LookoutEditorDialog extends DialogFragment {

    private View v;
    private EditText etName;
    private EditText etLat;
    private EditText etLon;
    private Button btnSave;
    private Button btnCancel;
    private Button btnDelete;

    private String lookoutName;
    private int position;
    private OnAddCrossListener onAddCrossListener;


    public LookoutEditorDialog() {
        // Required empty public constructor
    }

    public void setArgs(String lookoutName, int position, OnAddCrossListener onAddCrossListener){
        this.lookoutName = lookoutName;
        this.position = position;
        this.onAddCrossListener = onAddCrossListener;
    }

    public void setArgs(int position, OnAddCrossListener onAddCrossListener){
        this.position = position;
        this.onAddCrossListener = onAddCrossListener;
    }

    /**
     * Gets view references, fills in fields that we already have stored
     * information for, sets listeners for buttons
     *
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
        v = inflater.inflate(R.layout.fragment_lookout_editor_dialog, container, false);

        etName = (EditText) v.findViewById(R.id.et_lookout_name);
        etLat = (EditText) v.findViewById(R.id.et_lookout_lat);
        etLon = (EditText) v.findViewById(R.id.et_lookout_lon);
        setOnFocusChangeListener(etName);
        setOnFocusChangeListener(etLat);
        setOnFocusChangeListener(etLon);
        btnSave = (Button) v.findViewById(R.id.btn_save);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel);
        btnDelete = (Button) v.findViewById(R.id.btn_delete);
        if(lookoutName == null){
            btnDelete.setVisibility(View.GONE);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEntry();
            }
        });

        fillInFieldsIfPossible();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(view);
                dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fieldsFilledOut()) {
                    saveLookout();
                    onAddCrossListener.crossAdded();
                    hideSoftKeyboard(view);
                    dismiss();
                }else{
                    showErrorMessage();
                }
            }
        });

        return v;
    }

    private void setOnFocusChangeListener(EditText et){
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideSoftKeyboard(v);
                }
            }
        });
    }

    private void hideSoftKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private boolean fieldsFilledOut(){
        if(etLat.getText().toString().matches(Constants.DECIMAL_REGEX)
                && etLat.getText().toString().matches(Constants.DECIMAL_REGEX)
                && !etName.getText().toString().equals("")
                && !etLat.getText().toString().equals("")
                && !etLon.getText().toString().equals("")){
            return true;

        }
        return false;
    }

    private void showErrorMessage(){
        HomeScreen screen = (HomeScreen) getActivity();
        screen.showBasicErrorMessage(getString(R.string.fields_empty));
    }

    /**
     * Saves lookout info into SharedPreferences
     */
    private void saveLookout(){

        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(lookoutName + PreferencesKeys.LAT);
        editor.remove(lookoutName + PreferencesKeys.LON);

        editor.commit();

        String name = etName.getText().toString();
        Float lat = Float.parseFloat(etLat.getText().toString());
        Float lon = Float.parseFloat(etLon.getText().toString());

        editor.putString(PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + position, name);
        editor.putFloat(name + PreferencesKeys.LAT, lat);
        editor.putFloat(name + PreferencesKeys.LON, lon);

        editor.commit();

    }

    /**
     * Fills in name, elevation, and lat long text fields if information is already
     * stored in SharedPreferences
     */
    private void fillInFieldsIfPossible(){
        if(lookoutName != null){
            SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
            etName.setText(lookoutName);
            Float latitude = prefs.getFloat(lookoutName + PreferencesKeys.LAT, 0);
            Float longitude = prefs.getFloat(lookoutName + PreferencesKeys.LON, 0);
            if(latitude != 0){
                etLat.setText("" + latitude);
            }if(longitude != 0){
                etLon.setText("" + longitude);
            }
        }
    }

    /**
     * Deletes an entry from the cross lookouts
     */
    private void deleteEntry(){
        onAddCrossListener.crossDeleted(position);
        hideSoftKeyboard(v);
        dismiss();
    }


}
