package com.flightontrack.flight;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.model.EntityFlightHist;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.mysql.SQLFlightControllerEntity;
import com.flightontrack.mysql.SQLLocation;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.Props;

import static com.flightontrack.definitions.EventEnums.EVENT;
import static com.flightontrack.definitions.Finals.FLIGHT_NUMBER_DEFAULT;
import static com.flightontrack.definitions.Finals.ROUTE_NUMBER_DEFAULT;

public class EntityFlightControl {
    static final String TAG = "EntityFlightControl";
    public EntityFlightHist entityFlightHist;

    public enum FLIGHT_STATE {
        DEFAULT,
        GETTINGFLIGHT,
        READY_TOSAVELOCATIONS,
        INFLIGHT_SPEEDABOVEMIN,
        STOPPED,
        READY_TOBECLOSED,
        CLOSING,
        CLOSED
    }
    public enum FLIGHTNUMBER_SRC {
        REMOTE_DEFAULT,
        LOCAL
    }

    public String flightNumber = FLIGHT_NUMBER_DEFAULT;
    public String routeNumber;
    public int legNumber;

    public void setFlightState(FLIGHT_STATE fs) {
        if (this.flightState == fs) return;
        this.flightState = fs;
        sqlFlightControllerEntity.updateFlightState(dbid,fs.name());
        flightControl.onFlightStateChanged();
    }
    public void setFlightState(FLIGHT_STATE fs, String reason) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightState reasoning : " + fs + ' ' + reason, 'd'));
        if (this.flightState == fs) return;
        this.flightState = fs;
        sqlFlightControllerEntity.updateFlightState(dbid,fs.name());
        flightControl.onFlightStateChanged();
    }

    public void setFlightNumber(String fn) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, " setFlightNumber: " + fn + " flightNumStatus: " + flightNumStatus, 'd'));
        flightNumber = fn;
        routeNumber = routeNumber.equals(ROUTE_NUMBER_DEFAULT)?fn:routeNumber;
        entityFlightHist.setFlightNumber(fn);
        //RouteControl.setLastKnownFlightNumber(fn);
        Props.SessionProp.pLastKnownFlightNumber=fn;
        sqlFlightControllerEntity.updateFlightNum(dbid,fn,routeNumber);
        if (sqlLocation.updateTempFlightNum(flightNumber, fn) > 0) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "setFlightNumber: replace  in location table: " + flightNumber+"->" +fn, 'd'));
        }
        else new FontLogAsync().execute(new EntityLogMessage(TAG, "setFlightNumber: nothing to replace in location table: " + flightNumber+"->" +fn, 'd'));
        setFlightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
    }

    public void setIsJunk(int ij) {
        this.isJunk = ij;
        sqlFlightControllerEntity.updateIsJunkFlag(dbid,isJunk);
    }

    public void removeMyself(){
        new FontLogAsync().execute(new EntityLogMessage(TAG, "removeMyself f: " + this.flightNumber + " dbid: "+ dbid, 'd'));
        sqlFlightControllerEntity.deleteFlightEntity(dbid);
        if (RouteControl.flightControlList != null) {
            RouteControl.flightControlList.remove(this);
            if (RouteControl.activeFlightControl == this) RouteControl.setActiveFlightControl(null);
        }

    }

    public FLIGHT_STATE flightState = FLIGHT_STATE.DEFAULT;

    public void setFlightNumStatus(FLIGHTNUMBER_SRC fns) {
        this.flightNumStatus = fns;
        sqlFlightControllerEntity.updateFlightNumStatus(dbid,fns.name());
        switch (fns) {
            case REMOTE_DEFAULT:
                EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_REMOTENUMBER_RECEIVED)
                        .setEventMessageValueObject(this)
                        .setEventMessageValueString(flightNumber)
                );
                break;
            case LOCAL:
                break;
        }
    }

    public FLIGHTNUMBER_SRC flightNumStatus = FLIGHTNUMBER_SRC.REMOTE_DEFAULT;

    boolean isLimitReached  = false;
    public int    isJunk = 0;
    public int    dbid;

    SQLFlightControllerEntity sqlFlightControllerEntity = new SQLFlightControllerEntity();
    SQLLocation sqlLocation = SQLLocation.getInstance();
    FlightControl flightControl;

    EntityFlightControl() {
    }
    EntityFlightControl(String rn, int leg) {
        routeNumber = rn;
        legNumber = leg;
        dbid = sqlFlightControllerEntity.insertFlightControllerEntityRecord(this);
        //flightControl = new FlightControl(this);
        //this.entityFlight = flightControl.entityFlight;
        //setFlightState(FLIGHT_STATE.GETTINGFLIGHT);
    }

}
