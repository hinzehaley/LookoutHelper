package hinzehaley.com.lookouthelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import hinzehaley.com.lookouthelper.DialogFragments.SettingsDialog;
import hinzehaley.com.lookouthelper.fragments.InfoReportFragment;
import hinzehaley.com.lookouthelper.fragments.ConverterFragment;
import hinzehaley.com.lookouthelper.fragments.CrossLookoutFragment;
import hinzehaley.com.lookouthelper.fragments.HomeFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/*
TODO:
Test cross feature
Add a good tutorial
UI improvements
Test it all
Store reported fires -- make it possible to update them?

 */

/**
 * Only Activity, controls which fragments are visible
 */

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        HomeFragment homeFragment = HomeFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, homeFragment);
        transaction.commit();

        //detects if user hasn't entered lookout location, shows dialog to get info
        if(!lookoutInfoSet()){
            showSettingsDialog();
        }
    }

    /**
     * @return false if lookout info not set. True otherwise
     */
    public boolean lookoutInfoSet(){
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        if(prefs.getString(PreferencesKeys.STATE_PREFERENCES_KEY, null) == null){
            return false;
        }
        if(prefs.getFloat(PreferencesKeys.LOOKOUT_LAT_PREFERENCES_KEY, 0) == 0){
            return false;
        }
        if(prefs.getFloat(PreferencesKeys.LOOKOUT_LON_PREFERENCES_KEY, 0) == 0){
            return false;
        }
        if(prefs.getFloat(PreferencesKeys.LOOKOUT_ELEVATION_PREFERENCES_KEY, -1) == -1){
            return false;
        }
        if(prefs.getInt(PreferencesKeys.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1) == -1){
            return false;
        }
        return true;
    }

    /**
     * Shows dialog for user to input lookout info
     */
    private void showSettingsDialog(){
        if(getSupportFragmentManager().findFragmentByTag("SettingsDialog")==null) {
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
        }if (id == R.id.action_cross_lookouts) {
            showCrossLookoutEditorFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows fragment to input legal or lat lng and convert
     */
    public void goToConverterFragment(){
        ConverterFragment converterFragment = ConverterFragment.newInstance();
        replaceMainFragment(converterFragment);
    }

    /**
     * Shows fragment that displays conversions from legal to lat lng and vice versa
     * @param conversionsFragment
     */
    public void goToConversionsFragment(Fragment conversionsFragment){
        replaceMainFragment(conversionsFragment);
    }

    /**
     * Shows fragment displaying cross lookouts
     */
    private void showCrossLookoutEditorFragment(){
        CrossLookoutFragment crossLookoutFragment = CrossLookoutFragment.newInstance();
        replaceMainFragment(crossLookoutFragment);
    }

    /**
     * Replaces the main screen fragment with a new fragment
     * @param newFragment
     */
    private void replaceMainFragment(Fragment newFragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(" ");
        transaction.commit();
    }

    public void goToHomeFragment(){
        HomeFragment homeFragment = HomeFragment.newInstance();
        replaceMainFragment(homeFragment);
    }

    public void goToAzimuthFragment(){
        InfoReportFragment infoReportFragment = InfoReportFragment.newInstance();
        replaceMainFragment(infoReportFragment);
    }

    /**
     * Shows an error dialog with the option to go to settings or cancel
     * @param message
     */
    public void showErrorDialog(String message){
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
     * @param message
     */
    public void showBasicErrorMessage(String message){
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

    public void goToMapFragment(Fragment mapFragment){
        replaceMainFragment(mapFragment);
    }

    public boolean isConnectedToNetwork(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    /**
     * When back button pressed in menu bar, goes back to previous page
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
}
