package com.flightontrack.entities;

import com.flightontrack.shared.Const;
import com.loopj.android.http.RequestParams;


public class EntityRequestNewFlight  extends RequestParams {

    public EntityRequestNewFlight() {
        put("rcode", Const.REQUEST_FLIGHT_NUMBER);
    }

    public EntityRequestNewFlight set(String k, String v){
        put(k, v);
        return this;
    }
}
