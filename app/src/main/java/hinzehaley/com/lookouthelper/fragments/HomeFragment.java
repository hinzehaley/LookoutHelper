package hinzehaley.com.lookouthelper.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.R;

/**
 * Fragment that displays home screen with buttons to convert a geolocation descriptor
 * or to generate a fire report
 */
public class HomeFragment extends Fragment {

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Sets up view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_home, container, false);

        // removes back button from toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        final HomeScreen homeScreen = (HomeScreen) getActivity();

        Button btnConverter = (Button) v.findViewById(R.id.btn_converter);
        btnConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(homeScreen.lookoutInfoSet()) {
                    addConverterFragment();
                }else{
                    showErrorDialog(getString(R.string.no_lookout_set));
                }
            }
        });

        Button btnReportFire = (Button) v.findViewById(R.id.btn_report);
        btnReportFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(homeScreen.lookoutInfoSet()) {
                    addInfoReportFragment();
                }else{
                    showErrorDialog(getString(R.string.no_lookout_set));
                }
            }
        });

        final LinearLayout tutorial = (LinearLayout) v.findViewById(R.id.tutorial_home);

        ImageButton btnHelp = (ImageButton) v.findViewById(R.id.btn_question);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorial.setVisibility(View.VISIBLE);
            }
        });

        Button btnExitTutorial = (Button) v.findViewById(R.id.btn_exit_tutorial);
        btnExitTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorial.setVisibility(View.GONE);
            }
        });

        return v;
    }



    /**
     * Shows an error dialog with a given message and an ok button that dismisses
     * the dialog
     * @param message
     */
    private void showErrorDialog(String message){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.showErrorDialog(message);
    }

    /**
     * Adds the ConverterFragment to screen
     */
    private void addConverterFragment(){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.goToConverterFragment();
    }

    /**
     * Adds InfoReportFragment to screen
     */
    private void addInfoReportFragment(){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.goToAzimuthFragment();
    }

}
