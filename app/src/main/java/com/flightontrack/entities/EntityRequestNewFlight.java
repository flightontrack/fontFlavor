package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Const;
import com.loopj.android.http.RequestParams;


public class EntityRequestNewFlight  extends RequestParams implements AutoCloseable {
    static final String TAG = "EntityRequestNewFlight";

    public EntityRequestNewFlight() {
        put("rcode", Const.REQUEST_FLIGHT_NUMBER);
    }

    public EntityRequestNewFlight set(String k, String v){
        put(k, v);
        return this;
    }
    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'e'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
