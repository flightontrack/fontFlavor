package com.flightontrack.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.util.List;

import com.flightontrack.R;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityFlight;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.mysql.SQLFlightEntity;


public class FlightHistoryActivity extends Activity {

    private static final String TAG = "FlightHistoryActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button clearHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "FlightHistorytActivity onCreate", 'd'));
        setContentView(R.layout.activity_h);
        List<EntityFlight> flightList = new SQLFlightEntity().getFlightHistList();
        mRecyclerView = findViewById(R.id.my_recycler_view);
        clearHistory = findViewById(R.id.btnClearHist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerViewAdapter(flightList);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.acraft, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void  clearHistory(View v){
        new SQLFlightEntity().clearFlightEntityTable();
        finish();
    }
}



