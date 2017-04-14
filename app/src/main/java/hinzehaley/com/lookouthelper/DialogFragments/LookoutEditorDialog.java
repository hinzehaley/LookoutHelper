package hinzehaley.com.lookouthelper.DialogFragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import hinzehaley.com.lookouthelper.Constants;
import hinzehaley.com.lookouthelper.Interfaces.OnAddCrossListener;
import hinzehaley.com.lookouthelper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LookoutEditorDialog extends DialogFragment {

    private View v;
    private EditText etName;
    private EditText etLat;
    private EditText etLon;
    private Button btnSave;
    private Button btnCancel;
    private Button btnDelete;

    private String lookoutName;
    private int position;
    private OnAddCrossListener onAddCrossListener;


    public LookoutEditorDialog() {
        // Required empty public constructor
    }

    public void setArgs(String lookoutName, int position, OnAddCrossListener onAddCrossListener){
        this.lookoutName = lookoutName;
        this.position = position;
        this.onAddCrossListener = onAddCrossListener;
    }

    public void setArgs(int position, OnAddCrossListener onAddCrossListener){
        this.position = position;
        this.onAddCrossListener = onAddCrossListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_lookout_editor_dialog, container, false);

        etName = (EditText) v.findViewById(R.id.et_lookout_name);
        etLat = (EditText) v.findViewById(R.id.et_lookout_lat);
        etLon = (EditText) v.findViewById(R.id.et_lookout_lon);
        btnSave = (Button) v.findViewById(R.id.btn_save);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel);
        btnDelete = (Button) v.findViewById(R.id.btn_delete);
        if(lookoutName == null){
            btnDelete.setVisibility(View.GONE);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEntry();
            }
        });

        fillInFieldsIfPossible();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLookout();
                onAddCrossListener.crossAdded();
                dismiss();
            }
        });

        return v;
    }

    private void saveLookout(){

        Log.i("Saving lookout", "position : " + position + " NAme: " + lookoutName);

        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(lookoutName + "lat");
        editor.remove(lookoutName + "lon");

        editor.commit();

        String name = etName.getText().toString();
        Float lat = Float.parseFloat(etLat.getText().toString());
        Float lon = Float.parseFloat(etLon.getText().toString());

        editor.putString(Constants.CROSS_LOOKOUT_PREFERENCES_KEY + position, name);
        editor.putFloat(name + "lat", lat);
        editor.putFloat(name + "lon", lon);

        editor.commit();

    }

    private void fillInFieldsIfPossible(){
        if(lookoutName != null){
            SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
            etName.setText(lookoutName);
            Float latitude = prefs.getFloat(lookoutName + "lat", 0);
            Float longitude = prefs.getFloat(lookoutName + "lon", 0);
            if(latitude != 0){
                etLat.setText("" + latitude);
            }if(longitude != 0){
                etLon.setText("" + longitude);
            }
        }
    }

    private void deleteEntry(){
        onAddCrossListener.crossDeleted(position);
        dismiss();
    }


}
