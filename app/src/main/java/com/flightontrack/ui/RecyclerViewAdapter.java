package com.flightontrack.ui;
//import android.support.v7.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flightontrack.R;
import com.flightontrack.model.EntityFlightHist;

import java.util.List;

import static com.flightontrack.shared.Props.ctxApp;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>  {
    private final List<EntityFlightHist> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewAcft;
        public TextView textViewFlightNum;
        public TextView textViewRouteNum;
        public TextView textViewDate;
        public TextView textViewStartTime;
        public TextView textViewDuration;
        public MyViewHolder(View v) {
            super(v);
            //textViewFlightNum = v;
            textViewFlightNum = v.findViewById(R.id.id_oval);
            textViewAcft = v.findViewById(R.id.item_acft);
            textViewDate = v.findViewById(R.id.item_date);
            textViewStartTime = v.findViewById(R.id.item_starttime);
            textViewDuration = v.findViewById(R.id.item_dur);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapter(List<EntityFlightHist> list) {
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
        String fn = ctxApp.getString(R.string.label_hflight)+"\n"+ mDataset.get(position).flightNumber;
        String acft = mDataset.get(position).flightAcft;
        String ts = mDataset.get(position).flightTimeStart;
        String dt = mDataset.get(position).flightDate;
        String dur = mDataset.get(position).flightDuration;

        holder.textViewFlightNum.setText(fn);
        holder.textViewAcft.setText(acft);
        holder.textViewStartTime.setText(ts);
        holder.textViewDate.setText(dt);
        holder.textViewDuration.setText(dur);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
