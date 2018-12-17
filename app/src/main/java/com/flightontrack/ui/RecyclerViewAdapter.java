package com.flightontrack.ui;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flightontrack.R;
import com.flightontrack.model.EntityFlight;
import com.flightontrack.shared.GetTime;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements GetTime {
    private List<EntityFlight> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewAcft;
        public TextView textViewFlightNum;
        public TextView textViewRouteNum;
        public TextView textViewStartTime;
        public TextView textViewDuration;
        public MyViewHolder(View v) {
            super(v);
            //textViewFlightNum = v;
            textViewFlightNum = v.findViewById(R.id.id_oval);
            textViewAcft = v.findViewById(R.id.item_acft);
            textViewStartTime = v.findViewById(R.id.item_starttime);
            textViewDuration = v.findViewById(R.id.item_dur);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapter(List<EntityFlight> list) {
        mDataset = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textViewFlightNum.setText(holder.textViewFlightNum.getText()+"\n"+ mDataset.get(position).flightNumber);
        holder.textViewAcft.setText(holder.textViewAcft.getText()+ mDataset.get(position).flightAcft);
        holder.textViewStartTime.setText(holder.textViewStartTime.getText()+ getDateLocal(mDataset.get(position).flightTimeStart));
        holder.textViewDuration.setText(holder.textViewDuration.getText()+ mDataset.get(position).flightDuration);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
