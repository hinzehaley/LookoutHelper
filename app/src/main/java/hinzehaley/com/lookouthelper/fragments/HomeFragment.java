package hinzehaley.com.lookouthelper.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_home, container, false);

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
                    addAzimuthFragment();
                }else{
                    showErrorDialog(getString(R.string.no_lookout_set));
                }
            }
        });

        return v;
    }

    private void showErrorDialog(String message){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.showErrorDialog(message);
    }

    private void addConverterFragment(){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.goToConverterFragment();
    }

    private void addAzimuthFragment(){
        HomeScreen homeScreen = (HomeScreen) getActivity();
        homeScreen.goToAzimuthFragment();
    }

}
