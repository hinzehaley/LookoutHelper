package hinzehaley.com.lookouthelper.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import hinzehaley.com.lookouthelper.Interfaces.OnAddCrossListener;
import hinzehaley.com.lookouthelper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CrossLookoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrossLookoutFragment extends Fragment implements OnAddCrossListener{

    private ListView lstLookout;
    private Button btnAddLookout;
    private Button btnDone;
    private View v;
    private ArrayAdapter<String> adapter;

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

        lstLookout = (ListView) v.findViewById(R.id.lst_cross_lookouts);
        btnAddLookout = (Button) v.findViewById(R.id.btn_add);
        btnDone = (Button) v.findViewById(R.id.btn_done);

        setUpListOfLookouts();
        monitorListItemClicks();

        btnAddLookout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddLookoutDialog();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
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
        lookoutEditorDialog.setArgs(adapter.getCount(), this);
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
            String lookoutKey = Constants.CROSS_LOOKOUT_PREFERENCES_KEY + lookoutNumber;
            String lookoutName = prefs.getString(lookoutKey, null);
            if(lookoutName != null){
                lookoutNames.add(lookoutName);
                Log.i("adding name", lookoutName);
            }else{
                gettingLookouts = false;
            }
            lookoutNumber += 1;
        }


        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, lookoutNames);
        Log.i("adapter size", "" + adapter.getCount());
        lstLookout.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void monitorListItemClicks(){
        lstLookout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String lookoutName = adapter.getItem(i);
                showLookoutEditorDialog(lookoutName, i);

            }
        });
    }

    private void removeItem(int i){
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String name = prefs.getString(Constants.CROSS_LOOKOUT_PREFERENCES_KEY + i, null);
        if(name != null){
            editor.remove(Constants.CROSS_LOOKOUT_PREFERENCES_KEY + (adapter.getCount() - 1));
            adapter.remove(name);
            editor.remove(Constants.CROSS_LOOKOUT_PREFERENCES_KEY + i);
            editor.remove(name + "lat");
            editor.remove(name + "lon");

            for(int j = 0; j< adapter.getCount(); j++){
                editor.putString(Constants.CROSS_LOOKOUT_PREFERENCES_KEY + j, adapter.getItem(j));
            }
        }
        editor.commit();
    }


    @Override
    public void crossAdded() {
        setUpListOfLookouts();
    }

    @Override
    public void crossDeleted(int i) {
        removeItem(i);
    }
}
