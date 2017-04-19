package hinzehaley.com.lookouthelper.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import hinzehaley.com.lookouthelper.Interfaces.LookoutClickListener;
import hinzehaley.com.lookouthelper.R;

/**
 * Created by haleyhinze on 4/17/17.
 * Custom adapter for the RecyclerView displaying cross lookouts
 */

public class LookoutAdapter extends RecyclerView.Adapter {

    ArrayList<String> lookouts;
    LookoutClickListener listener;


    public LookoutAdapter(ArrayList<String> lookouts, LookoutClickListener listener){
        this.lookouts = lookouts;
        this.listener = listener;
    }

    /**
     * Creates a new view and a view holder for that view
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cross, parent, false);
        return new CustomHolder(itemView);
    }

    /**
     * Sets relevant fields for view holder -- holder may be recycled
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String lookout = lookouts.get(position);
        CustomHolder myHolder = (CustomHolder) holder;
        myHolder.txtLookoutName.setText(lookout);
        myHolder.index = position;
    }

    @Override
    public int getItemCount() {
        return lookouts.size();
    }

    public void remove(String name){
        lookouts.remove(name);
    }

    public String getItem(int index){
        return lookouts.get(index);
    }

    /**
     * Custom view holder that contains its index in the array and
     * a TextView containing the name of the lookout
     */
    class CustomHolder extends RecyclerView.ViewHolder{

        public TextView txtLookoutName;
        public int index;

        public CustomHolder(View itemView) {
            super(itemView);
            txtLookoutName = (TextView) itemView.findViewById(R.id.txt_lookout_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.lookoutClicked((String) txtLookoutName.getText(), index);
                }
            });

        }
    }


}
