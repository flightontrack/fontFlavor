package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.definitions.Finals;
import com.loopj.android.http.RequestParams;


public class EntityRequestNewFlightOffline  extends RequestParams  implements AutoCloseable{
    static final String TAG = "EntityRequestNewFlightOffline";

    public EntityRequestNewFlightOffline() {
        put("rcode", Finals.REQUEST_FLIGHT_NUMBER);
    }

    public EntityRequestNewFlightOffline set(String k, String v){
        put(k, v);
        return this;
    }
    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'e'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
