package com.flightontrack.model;

import com.flightontrack.definitions.Finals;
import com.flightontrack.shared.Props;
import com.loopj.android.http.RequestParams;

public class EntityRequestPostLocation   extends RequestParams implements AutoCloseable{
    final String TAG = "EntityRequestPostLocation";

    public EntityRequestPostLocation(EntityLocation l) {
        put("rcode", Finals.REQUEST_LOCATION_UPDATE);
        put("isdebug", Props.SessionProp.pIsDebug);
        put("speedlowflag", l.sl == 1);
        put("rcode", l.rc);
        put("latitude", l.la);
        put("longitude", l.lo);
        put("flightid", l.ft);
        put("accuracy", l.ac);
        put("extrainfo", l.al);
        put("wpntnum", l.wp);
        put("gsmsignal", l.sg);
        put("speed", l.sd);
        put("dtime", l.dt);
        put("elevcheck", l.irch == 1);
    }

    public EntityRequestPostLocation set(String k, String v){
        put(k, v);
        return this;
    }

    @Override
    public void close() {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
