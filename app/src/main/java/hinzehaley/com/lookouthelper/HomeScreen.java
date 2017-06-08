package hinzehaley.com.lookouthelper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import hinzehaley.com.lookouthelper.DialogFragments.SettingsDialog;
import hinzehaley.com.lookouthelper.fragments.InfoReportFragment;
import hinzehaley.com.lookouthelper.fragments.ConverterFragment;
import hinzehaley.com.lookouthelper.fragments.CrossLookoutFragment;
import hinzehaley.com.lookouthelper.fragments.HomeFragment;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/*
TODO:
Test it all

 */

/**
 * Only Activity, controls which fragments are visible
 */

public class HomeScreen extends AppCompatActivity implements LocationListener{

    public Location mLastLocation;
    public boolean isUsingLocation = false;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    public static final int REQUEST_PERMISSIONS_LOCATION = 5;
    boolean justRequestedLocationPermissions = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isUsingLocation = shouldBeMonitoringLocation();

        HomeFragment homeFragment = HomeFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, homeFragment);
        transaction.commit();

        //detects if user hasn't entered lookout location, shows dialog to get info
        if (!lookoutInfoSet()) {
            showSettingsDialog();
        }

        buildLocationRequest();
        setUpGoogleApiClient();

    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(!justRequestedLocationPermissions) {
                justRequestedLocationPermissions = true;
                requestLocationPermissions();
            }else{
                justRequestedLocationPermissions = false;
            }
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void requestLocationPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(shouldBeMonitoringLocation()){
                        startLocationUpdates();
                    }

                } else {
                    justRequestedLocationPermissions = true;
                    showBasicErrorMessage(getString(R.string.needs_location_permissions));

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setUpGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if(shouldBeMonitoringLocation()){
                            startLocationUpdates();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        showErrorDialog(getString(R.string.google_client_error));
                    }
                })
                .build();
    }

    /**
     *
     * @return true if we should be monitoring user's location, false otherwise
     */
    private boolean shouldBeMonitoringLocation(){
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getBoolean(PreferencesKeys.USE_LOCATION, false);
    }

    /**
     * @return false if lookout info not set. True otherwise
     */
    public boolean lookoutInfoSet() {
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        if (prefs.getString(PreferencesKeys.STATE_PREFERENCES_KEY, null) == null) {
            return false;
        }
        if (prefs.getInt(PreferencesKeys.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1) == -1) {
            return false;
        }

        if(!prefs.getBoolean(PreferencesKeys.USE_LOCATION, false)) {

            if (prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 100000) == 100000) {
                return false;
            }
            if (prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 100000) == 100000) {
                return false;
            }
            if (prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, -100000) == -100000) {
                return false;
            }
        }

        return true;
    }

    /**
     * Shows dialog for user to input lookout info
     */
    private void showSettingsDialog() {
        if (getSupportFragmentManager().findFragmentByTag("SettingsDialog") == null) {
            DialogFragment newFragment = new SettingsDialog();
            newFragment.show(getSupportFragmentManager(), "SettingsDialog");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    /**
     * Sets up menu items
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        if (id == R.id.action_cross_lookouts) {
            showCrossLookoutEditorFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows fragment to input legal or lat lng and convert
     */
    public void goToConverterFragment() {
        ConverterFragment converterFragment = ConverterFragment.newInstance();
        replaceMainFragment(converterFragment);
    }

    /**
     * Shows fragment that displays conversions from legal to lat lng and vice versa
     *
     * @param conversionsFragment
     */
    public void goToConversionsFragment(Fragment conversionsFragment) {
        replaceMainFragment(conversionsFragment);
    }

    /**
     * Shows fragment displaying cross lookouts
     */
    private void showCrossLookoutEditorFragment() {
        CrossLookoutFragment crossLookoutFragment = CrossLookoutFragment.newInstance();
        replaceMainFragment(crossLookoutFragment);
    }

    /**
     * Replaces the main screen fragment with a new fragment
     *
     * @param newFragment
     */
    private void replaceMainFragment(Fragment newFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(" ");
        transaction.commit();
    }

    public void goToHomeFragment() {
        HomeFragment homeFragment = HomeFragment.newInstance();
        replaceMainFragment(homeFragment);
    }

    public void goToAzimuthFragment() {
        InfoReportFragment infoReportFragment = InfoReportFragment.newInstance();
        replaceMainFragment(infoReportFragment);
    }

    /**
     * Shows an error dialog with the option to go to settings or cancel
     *
     * @param message
     */
    public void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                getString(R.string.settings),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        showSettingsDialog();
                    }
                });

        builder.setNegativeButton(
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();

                    }
                }
        );

        AlertDialog errorDialog = builder.create();
        errorDialog.show();
    }

    /**
     * Shows an error dialog with the provided message and an OK button that cancels the dialog
     *
     * @param message
     */
    public void showBasicErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog errorDialog = builder.create();
        errorDialog.show();
    }

    public void goToMapFragment(Fragment mapFragment) {
        replaceMainFragment(mapFragment);
    }

    public boolean isConnectedToNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    /**
     * When back button pressed in menu bar, goes back to previous page
     *
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        if(isUsingLocation) {
            stopLocationMonitoring();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        if(isUsingLocation) {
            startLocationMonitoring();
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient.isConnected()){

            startLocationUpdates();
        }
    }

    public void startLocationMonitoring(){
        if(mGoogleApiClient.isConnected()){
            startLocationUpdates();
        }else {
            mGoogleApiClient.connect();
        }

    }

    public void buildLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void stopLocationMonitoring(){
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void setIsUsingLocation(boolean usingLocation){
        if(usingLocation){
            isUsingLocation = usingLocation;
            startLocationMonitoring();
        }
        if(isUsingLocation && !usingLocation){
            isUsingLocation = usingLocation;
            stopLocationMonitoring();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }


}
