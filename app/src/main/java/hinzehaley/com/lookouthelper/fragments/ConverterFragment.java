package hinzehaley.com.lookouthelper.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.PreferencesKeys;
import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.models.GeoConverter;

/**
 * Fragment for user to input lat lng or legal. Starts requesting conversions, which will be
 * shown in ShowConversionsFragment.
 */
public class ConverterFragment extends Fragment {

    private EditText etTownship;
    private EditText etRange;
    private EditText etSection;
    private EditText etQuarterSection;
    private Spinner spinnerTownship;
    private Spinner spinnerRange;
    private Button btnSubmit;
    private EditText etLat;
    private EditText etLon;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment ConverterFragment.
     */
    public static ConverterFragment newInstance() {
        ConverterFragment fragment = new ConverterFragment();
        return fragment;
    }

    public ConverterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Sets up UI
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_converter, container, false);
        etTownship = (EditText) v.findViewById(R.id.et_township);
        etRange = (EditText) v.findViewById(R.id.et_range);
        etSection = (EditText) v.findViewById(R.id.et_section);
        etQuarterSection = (EditText) v.findViewById(R.id.et_quarter_section);
        spinnerTownship = (Spinner) v.findViewById(R.id.spinner_township);
        spinnerRange = (Spinner) v.findViewById(R.id.spinner_range);
        btnSubmit = (Button) v.findViewById(R.id.btn_convert);
        etLat = (EditText) v.findViewById(R.id.et_lat);
        etLon = (EditText) v.findViewById(R.id.et_lon);


        fillInSpinners();

        setAllListeners();

        setupUI(v);

        //Makes back button visible in toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        return v;
    }

    /**
     * Fills in spinners for cardinal directions on legal description
     */
    private void fillInSpinners(){
        ArrayAdapter<CharSequence> adapterTownship = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_north_south, android.R.layout.simple_spinner_item);
        adapterTownship.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTownship.setAdapter(adapterTownship);

        ArrayAdapter<CharSequence> adapterRange = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_east_west, android.R.layout.simple_spinner_item);
        adapterRange.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRange.setAdapter(adapterRange);
    }

    /**
     * Listens for changes to EditTexts
     */
    private void setAllListeners(){
        setListenerEditText(etTownship);
        setListenerEditText(etRange);
        setListenerEditText(etSection);
        setListenerEditText(etLat);
        setListenerEditText(etLon);
    }


    /**
     * @param et
     */
    private void setListenerEditText(EditText et){
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                makeSubmitButtonClickableIfNecessary();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    /**
     * Checks if either all the fields for lat lng are entered or all fields for legal
     * are entered
     * @return true if all information necessary is provided for either lat lng or legal
     */
    private boolean necessaryFieldsFull(){
        if(legalEntered()){
            return true;
        }
        else if(latLonEntered()){
            return true;
        }
        return false;
    }

    /**
     * @param et
     * @return true if et contains text, false if et is empty
     */
    private boolean editTextContainsText(EditText et){
        if(et.getText() != null){
            if(!et.getText().toString().equals("")){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if township, range, and section are entered, false otherwise
     * @return
     */
    private boolean legalEntered(){
        if(editTextContainsText(etTownship) && editTextContainsText(etRange) && editTextContainsText(etSection)){
            return true;
        }
        return false;
    }

    /**
     * Returns true if lat and lng are entered, false otherwise
     * @return
     */
    private boolean latLonEntered(){

        if(editTextContainsText(etLat) && editTextContainsText(etLon)){
            return true;
        }
        return false;
    }



    /**
     * Makes submit button clickable if necessary fields are filled in.
     * Listens for clicks on submit button. If clicked, uses the geoConverter
     * to convert the entered location type into the other location type
     * (legal -- lat lng). GeoConverter is passed an instance of showConversionsFragment
     * to use for displaying results
     */
    private void makeSubmitButtonClickableIfNecessary(){
        if(necessaryFieldsFull()){
            btnSubmit.setClickable(true);
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GeoConverter geoConverter = new GeoConverter();
                    ShowConversionsFragment showConversionsFragment = new ShowConversionsFragment();
                    if(legalEntered()) {

                        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
                        String state = prefs.getString(PreferencesKeys.STATE_PREFERENCES_KEY, null);
                        int principalMeridian = prefs.getInt(PreferencesKeys.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1);
                        try {
                            geoConverter.requestLocationFromLegal(showConversionsFragment, state, principalMeridian, Integer.parseInt(etTownship.getText().toString()),
                                    Integer.parseInt(etRange.getText().toString()),
                                    Integer.parseInt(etSection.getText().toString()),
                                    etQuarterSection.getText().toString(), spinnerTownship.getSelectedItem().toString(), spinnerRange.getSelectedItem().toString(), getContext());
                            //hide soft keyboard
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            HomeScreen homeScreen = (HomeScreen) getActivity();
                            homeScreen.goToConversionsFragment(showConversionsFragment);
                        }catch (IllegalArgumentException e){
                            HomeScreen homeScreen = (HomeScreen) getActivity();
                            homeScreen.showBasicErrorMessage(getString(R.string.legal_wrong_format));
                        }
                    }else if(latLonEntered()){
                        try {
                            geoConverter.requestLegalFromLocation(showConversionsFragment,
                                    Location.convert(etLat.getText().toString()),
                                    Location.convert(etLon.getText().toString()), getContext());
                            //hide soft keyboard
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            HomeScreen homeScreen = (HomeScreen) getActivity();
                            homeScreen.goToConversionsFragment(showConversionsFragment);

                        }catch (IllegalArgumentException e){
                            HomeScreen homeScreen = (HomeScreen) getActivity();
                            homeScreen.showBasicErrorMessage(getString(R.string.coordinates_wrong_format));

                        }
                    }

                }
            });
        }
    }

    /**
     * Hides soft keyboard if non-edit text view is touched
     * @param view
     */
    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }

            });
        }

        //If a layout container, iterate over children
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

}
