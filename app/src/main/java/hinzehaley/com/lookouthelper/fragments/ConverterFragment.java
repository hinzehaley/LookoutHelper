package hinzehaley.com.lookouthelper.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.models.GeoConverter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConverterFragment#newInstance} factory method to
 * create an instance of this fragment.
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

        return v;
    }

    private void fillInSpinners(){
        ArrayAdapter<CharSequence> adapterTownship = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_north_south, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterTownship.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerTownship.setAdapter(adapterTownship);

        ArrayAdapter<CharSequence> adapterRange = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_east_west, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterRange.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerRange.setAdapter(adapterRange);
    }

    private void setAllListeners(){
        setListenerEditText(etTownship);
        setListenerEditText(etRange);
        setListenerEditText(etSection);
        setListenerEditText(etLat);
        setListenerEditText(etLon);
    }



    private void setListenerEditText(EditText et){
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                makeSubmitButtonClickableIfNecessary();
            }
        });
    }

    private boolean necessaryFieldsFull(){
        if(legalEntered()){
            return true;
        }
        else if(latLonEntered()){
            return true;
        }
        return false;
    }

    private boolean editTextContainsText(EditText et){
        if(et.getText() != null){
            if(!et.getText().toString().equals("")){
                Log.i("CONTAINS TEXT:", et.getText().toString());
                return true;
            }
        }
        return false;
    }

    private boolean legalEntered(){
        if(editTextContainsText(etTownship) && editTextContainsText(etRange) && editTextContainsText(etSection)){
            return true;
        }
        return false;
    }

    private boolean latLonEntered(){
        if(editTextContainsText(etLat) && editTextContainsText(etLon)){
            return true;
        }
        return false;
    }

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
                        String state = prefs.getString(Constants.STATE_PREFERENCES_KEY, null);
                        int principalMeridian = prefs.getInt(Constants.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1);
                        if((state == null) || principalMeridian == -1){
                            //TODO: SHOW ERROR ASKING TO EDIT SETTINGS
                            return;
                        }

                        geoConverter.requestLocationFromLegal(showConversionsFragment, state, principalMeridian, Integer.parseInt(etTownship.getText().toString()),
                                Integer.parseInt(etRange.getText().toString()),
                                Integer.parseInt(etSection.getText().toString()),
                                etQuarterSection.getText().toString(), spinnerTownship.getSelectedItem().toString(), spinnerRange.getSelectedItem().toString());
                    }else if(latLonEntered()){
                        geoConverter.requestLegalFromLocation(showConversionsFragment,
                                Location.convert(etLat.getText().toString()),
                                Location.convert(etLon.getText().toString()));
                    }
                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    HomeScreen homeScreen = (HomeScreen) getActivity();
                    homeScreen.goToConversionsFragment(showConversionsFragment);
                }
            });
        }
    }


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

        //If a layout container, iterate over children and seed recursion.
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
