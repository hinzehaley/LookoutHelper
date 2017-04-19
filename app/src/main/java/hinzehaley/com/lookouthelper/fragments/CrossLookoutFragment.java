package hinzehaley.com.lookouthelper.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.DialogFragments.LookoutEditorDialog;
import hinzehaley.com.lookouthelper.DialogFragments.SettingsDialog;
import hinzehaley.com.lookouthelper.Interfaces.LookoutClickListener;
import hinzehaley.com.lookouthelper.Interfaces.OnAddCrossListener;
import hinzehaley.com.lookouthelper.PreferencesKeys;
import hinzehaley.com.lookouthelper.R;
import hinzehaley.com.lookouthelper.adapters.LookoutAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CrossLookoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrossLookoutFragment extends Fragment implements OnAddCrossListener, LookoutClickListener{

    private RecyclerView lstLookout;
    private Button btnAddLookout;
    private View v;
    private LookoutAdapter adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CrossLookoutFragment.
     */
    public static CrossLookoutFragment newInstance() {
        CrossLookoutFragment fragment = new CrossLookoutFragment();
        return fragment;
    }

    public CrossLookoutFragment() {
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
        v = inflater.inflate(R.layout.fragment_cross_lookout, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        lstLookout = (RecyclerView) v.findViewById(R.id.lst_cross_lookouts);
        btnAddLookout = (Button) v.findViewById(R.id.btn_add);

        setUpListOfLookouts();

        btnAddLookout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddLookoutDialog();
            }
        });


        return v;
    }

    private void showLookoutEditorDialog(String lookoutName, int lookoutIndex){
        LookoutEditorDialog lookoutEditorDialog = new LookoutEditorDialog();
        lookoutEditorDialog.setArgs(lookoutName, lookoutIndex, this);
        showDialog(lookoutEditorDialog);
    }

    private void showAddLookoutDialog(){
        LookoutEditorDialog lookoutEditorDialog = new LookoutEditorDialog();
        lookoutEditorDialog.setArgs(adapter.getItemCount(), this);
        showDialog(lookoutEditorDialog);
    }


    private void showDialog(DialogFragment dialog){
        if(getActivity().getSupportFragmentManager().findFragmentByTag("CrossDialog")==null) {
            dialog.show(getActivity().getSupportFragmentManager(), "CrossDialog");
        }
    }


    private void goBack(){
        getActivity().onBackPressed();
    }


    private void setUpListOfLookouts(){
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);


        ArrayList<String> lookoutNames = new ArrayList();
        boolean gettingLookouts = true;
        int lookoutNumber = 0;
        while(gettingLookouts){
            String lookoutKey = PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + lookoutNumber;
            String lookoutName = prefs.getString(lookoutKey, null);
            if(lookoutName != null){
                lookoutNames.add(lookoutName);
                Log.i("adding name", lookoutName);
            }else{
                gettingLookouts = false;
            }
            lookoutNumber += 1;
        }


        adapter = new LookoutAdapter(lookoutNames, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        lstLookout.setLayoutManager(mLayoutManager);
        lstLookout.setItemAnimator(new DefaultItemAnimator());
        lstLookout.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    private void removeItem(int i){
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String name = prefs.getString(PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + i, null);
        if(name != null){
            editor.remove(PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + (adapter.getItemCount() - 1));
            adapter.remove(name);
            editor.remove(PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + i);
            editor.remove(name + PreferencesKeys.LAT);
            editor.remove(name + PreferencesKeys.LON);

            for(int j = 0; j< adapter.getItemCount(); j++){
                editor.putString(PreferencesKeys.CROSS_LOOKOUT_PREFERENCES_KEY + j, adapter.getItem(j));
            }
        }
        editor.commit();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void crossAdded() {
        setUpListOfLookouts();
    }

    @Override
    public void crossDeleted(int i) {
        removeItem(i);
    }

    @Override
    public void lookoutClicked(String name, int index) {
        showLookoutEditorDialog(name, index);
    }
}
