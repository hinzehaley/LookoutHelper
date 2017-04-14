package hinzehaley.com.lookouthelper.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import hinzehaley.com.lookouthelper.HomeScreen;
import hinzehaley.com.lookouthelper.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ShowConversionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowConversionsFragment extends Fragment {

    private View v;
    private Location location;
    private String legal;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShowConversionsFragment.
     */
    public static ShowConversionsFragment newInstance() {
        ShowConversionsFragment fragment = new ShowConversionsFragment();
        return fragment;
    }

    public ShowConversionsFragment() {
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
        v = inflater.inflate(R.layout.fragment_show_conversions, container, false);
        Log.i("CONVERSION", "onCreateView, ID: " + getId());

        if(location != null){
            Log.i("CONVERSION", "location notnull");

            displayLocation();
        }
        return v;
    }


    public void passInNewLocation(Location location, String legal){

        if(location == null){
            HomeScreen activity = (HomeScreen) getActivity();
            activity.showBasicErrorMessage(getString(R.string.could_not_parse_location));
            return;
        }

        this.location = location;
        this.legal = legal;
        if(v != null && getActivity() != null){
            displayLocation();
        }

    }


    public void passInNewLegal(Location location, String legal){

        if(legal == null){
            HomeScreen activity = (HomeScreen) getActivity();
            activity.showBasicErrorMessage(getString(R.string.could_not_parse_coordinates));
            return;
        }

        this.location = location;
        this.legal = legal;
        if(v != null && getActivity() != null){
            displayLocation();
        }
    }





    private void displayLocation(){
        Log.i("CONVERSION", "display location");

        if(location != null && v != null){
            LinearLayout layoutConversions = (LinearLayout) v.findViewById(R.id.lin_layout_conversions);
            TextView txtWait = (TextView) v.findViewById(R.id.txt_wait);
            TextView txtLatDecimalDegrees = (TextView) v.findViewById(R.id.txt_lat_decimal_degrees);
            TextView txtLonDecimalDegrees = (TextView) v.findViewById(R.id.txt_lon_decimal_degrees);
            TextView txtLatDecimalMinutes = (TextView) v.findViewById(R.id.txt_lat_decimal_minutes);
            TextView txtLonDecimalMinutes = (TextView) v.findViewById(R.id.txt_lon_decimal_minutes);
            TextView txtLatSeconds = (TextView) v.findViewById(R.id.txt_lat_seconds);
            TextView txtLonSeconds = (TextView) v.findViewById(R.id.txt_lon_seconds);
            TextView txtLegal = (TextView) v.findViewById(R.id.txt_legal);

            layoutConversions.setVisibility(View.VISIBLE);
            txtWait.setVisibility(View.GONE);

            String strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
            String strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);

            txtLatDecimalDegrees.setText(getString(R.string.lat) + strLatitude);
            txtLonDecimalDegrees.setText(getString(R.string.lon) + strLongitude);

            strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_MINUTES);
            strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_MINUTES);

            txtLatDecimalMinutes.setText(getString(R.string.lat) + strLatitude);
            txtLonDecimalMinutes.setText(getString(R.string.lon) + strLongitude);

            strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);
            strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);

            txtLatSeconds.setText(getString(R.string.lat) + strLatitude);
            txtLonSeconds.setText(getString(R.string.lon) + strLongitude);

            txtLegal.setText(legal.toUpperCase());

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(this.location != null && this.legal != null){
            displayLocation();
        }
    }
}
