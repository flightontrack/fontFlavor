package com.flightontrack.model;
import android.location.Location;

import static com.flightontrack.definitions.Enums.*;
import static com.flightontrack.control.Session.*;
import static com.flightontrack.control.RouteControl.*;
import static com.flightontrack.definitions.EventEnums.*;

/**
 * Created by hotvk on 12/28/2017.
 */

public class EntityEventMessage {
public EVENT event;
public boolean eventMessageValueBool;
public String eventMessageValueFlightNumString;
public String eventMessageValueString;
public int eventMessageValueInt;
public Object eventMessageValueObject;
public Location eventMessageValueLocation;
public MODE eventMessageValueClockMode;
public SACTION eventMessageValueSessionRequest;
public RACTION eventMessageValueRouteRequest;
public ALERT_RESPONSE eventMessageValueAlertResponse;
//public SVCCOMM_SUCCESS_TYPE eventMessageValueSvcCommSuccessType;

public EntityEventMessage(EVENT eventMessage ){
    this.event = eventMessage;
    //return this;
}
public EntityEventMessage setEventMessageValueBool(Boolean val){
    this.eventMessageValueBool = val;
    return this;
}
public EntityEventMessage setEventMessageValueLocation(Location val){
    this.eventMessageValueLocation = val;
    return this;
}
public EntityEventMessage setEventMessageValueClockMode(MODE val){
        this.eventMessageValueClockMode = val;
        return this;
    }
public EntityEventMessage setEventMessageValueSessionRequest(SACTION val){
    this.eventMessageValueSessionRequest = val;
    return this;
}
public EntityEventMessage setEventMessageValueRouteRequest(RACTION val){
    this.eventMessageValueRouteRequest = val;
    return this;
}
public EntityEventMessage setEventMessageValueAlertResponse(ALERT_RESPONSE val){
    this.eventMessageValueAlertResponse = val;
    return this;
}
public EntityEventMessage setEventMessageValueInt(int val){
    this.eventMessageValueInt= val;
    return this;
}
//public EventMessage setEventMessageValueSvcCommSuccessType(SVCCOMM_SUCCESS_TYPE val){
//    this.eventMessageValueSvcCommSuccessType= val;
//    return this;
//}
public EntityEventMessage setEventMessageValueString(String val){
    this.eventMessageValueString= val;
    return this;
}
    public EntityEventMessage setEventMessageValueFlightNumString(String val){
        this.eventMessageValueFlightNumString = val;
        return this;
    }
public EntityEventMessage setEventMessageValueObject(Object val){
    this.eventMessageValueObject= val;
    return this;
}
}
