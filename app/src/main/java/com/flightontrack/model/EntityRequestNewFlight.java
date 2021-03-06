package com.flightontrack.model;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.definitions.Finals;
import com.loopj.android.http.RequestParams;


public class EntityRequestNewFlight  extends RequestParams implements AutoCloseable {
    static final String TAG = "EntityRequestNewFlight";

    public EntityRequestNewFlight() {
        put("rcode", Finals.REQUEST_FLIGHT_NUMBER);
    }

    public EntityRequestNewFlight set(String k, String v){
        put(k, v);
        return this;
    }
    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG,"From close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
