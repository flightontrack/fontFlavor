package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.definitions.Finals;
import com.loopj.android.http.RequestParams;

public class EntityRequestCloseFlight   extends RequestParams implements AutoCloseable{
    final String TAG = "EntityRequestCloseFlight";
    final int rcode = Finals.REQUEST_STOP_FLIGHT;


    public EntityRequestCloseFlight() {
        put("rcode", rcode);
    }

    public EntityRequestCloseFlight set(String k, String v) {
        put(k, v);
        return this;
    }
    public EntityRequestCloseFlight set(String k, boolean v) {
        put(k, v);
        return this;
    }
    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}

