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
import hinzehaley.com.lookouthelper.fragments.AzimuthFragment;
import hinzehaley.com.lookouthelper.fragments.ConverterFragment;
import hinzehaley.com.lookouthelper.fragments.CrossLookoutFragment;
import hinzehaley.com.lookouthelper.fragments.HomeFragment;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);

        HomeFragment homeFragment = HomeFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, homeFragment);
        transaction.commit();

        if(prefs.getString("state", null) == null){
            showSettingsDialog();
        }



    }

    public boolean lookoutInfoSet(){
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        if(prefs.getString(Constants.STATE_PREFERENCES_KEY, null) == null){
            return false;
        }
        if(prefs.getFloat(Constants.LOOKOUT_LAT_PREFERENCES_KEY, 0) == 0){
            return false;
        }
        if(prefs.getFloat(Constants.LOOKOUT_LON_PREFERENCES_KEY, 0) == 0){
            return false;
        }
        if(prefs.getFloat(Constants.LOOKOUT_ELEVATION_PREFERENCES_KEY, -1) == -1){
            return false;
        }
        if(prefs.getInt(Constants.PRINCIPAL_MERIDIAN_PREFERENCES_KEY, -1) == -1){
            return false;
        }
        return true;
    }

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

    public void goToConverterFragment(){
        ConverterFragment converterFragment = ConverterFragment.newInstance();
        replaceMainFragment(converterFragment);
    }

    public void goToConversionsFragment(Fragment conversionsFragment){
        replaceMainFragment(conversionsFragment);
    }

    private void showCrossLookoutEditorFragment(){
        CrossLookoutFragment crossLookoutFragment = CrossLookoutFragment.newInstance();
        replaceMainFragment(crossLookoutFragment);
    }

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
        AzimuthFragment azimuthFragment = AzimuthFragment.newInstance();
        replaceMainFragment(azimuthFragment);
    }

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
}
