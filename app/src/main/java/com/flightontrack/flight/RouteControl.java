package com.flightontrack.flight;

import com.flightontrack.definitions.Limits;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.Props;

import java.util.ArrayList;

import static com.flightontrack.definitions.EventEnums.EVENT;
import static com.flightontrack.definitions.Finals.COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED;
import static com.flightontrack.definitions.Finals.COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN;
import static com.flightontrack.definitions.Finals.ROUTE_NUMBER_DEFAULT;

public class RouteControl implements EventBus{
    final String TAG = "RouteControl";

    public enum RACTION {
        OPEN_NEW_FLIGHT,
        //SWITCH_TO_PENDING,
        RESTART_NEW_FLIGHT,
        REMOVE_FLIGHT_IF_CLOSED,
        ADD_OR_UPDATE_FLIGHT

    }

    static RouteControl routeControlInstance = null;
    public static FlightControl activeFlightControl;
    public static ArrayList<FlightControl> flightControlList = new ArrayList<>();
    EntityEventMessage entityEventMessage;
    EVENT ev;

    public static RouteControl getInstance() {
        if(routeControlInstance == null) {
            routeControlInstance = new RouteControl();
        }
        return routeControlInstance;
    }

     public static FlightOffline get_FlightInstanceByNumber(String flightNumber){
        for (FlightOffline f : flightList) {
            if (f.entityFlight.flightNumber.equals(flightNumber)) {
                return f;
            }
        }
        return activeFlight;
    }
    public static boolean isFlightNumberInList(String flightNumber){
        for (FlightOffline f : flightList) {
            if (f.entityFlight.flightNumber.equals(flightNumber)) {
                return true;
            }
        }
        return false;
    }
    void setToNull(){
        RouteControl.activeFlightControl = null;
        //RouteControl.activeRoute = null;
        EventBus.distribute(new EntityEventMessage(EVENT.ROUTE_NOACTIVEROUTE));
    }
    void set_rAction(RACTION request) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "reaction:" + request, 'd'));
        switch (request) {
            case REMOVE_FLIGHT_IF_CLOSED:
                new FontLogAsync().execute(new EntityLogMessage(TAG, "REMOVE_FLIGHT_IF_CLOSED: flightList: size : " + flightList.size(), 'd'));
                    for (FlightOffline f : new ArrayList<>(flightList)) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "f:" + f.entityFlight.flightNumber + ":" + request, 'd'));
                        if (f.flightState.equals(FlightOffline.FLIGHT_STATE.CLOSED)) {
                            //if (activeFlight == f) activeFlight = null;
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "reaction:" + request+":f:"+f, 'd'));
                            if (f==activeFlight) activeFlight =null;
                            flightList.remove(f);
                        }
                        if (flightList.isEmpty()) {
                            //if (activeRoute == this) activeRoute = null;
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "flightList isEmpty", 'd'));
                            setToNull();
                        }
                    }
                break;
            case ADD_OR_UPDATE_FLIGHT:
                FlightOffline fb = (FlightOffline) entityEventMessage.eventMessageValueObject;
                new FontLogAsync().execute(new EntityLogMessage(TAG, "fb.fn"+fb.entityFlight.flightNumber, 'd'));
                //new FontLogAsync().execute(new LogMessage(TAG, "fb.fnt"+fb.flightNumberTemp, 'd');
                if (flightList.contains(fb)) break;
                else {
                    flightList.add((FlightOffline) entityEventMessage.eventMessageValueObject);
                    break;
                }
        }
    }

    private void restartNewFlight(){
        int legCount = activeFlightControl.legNumber++;
        if (Props.SessionProp.pIsMultileg && (legCount <= Limits.LEG_COUNT_HARD_LIMIT)) {
            flightControlList.add(new FlightControl(activeFlightControl.routeNumber,legCount));
        } else {
            /// keep clock ticking
            EventBus.distribute(new EntityEventMessage(EVENT.ROUTE_ONRESTART).setEventMessageValueBool(false));
        }
    }

    @Override
    public void onClock(EntityEventMessage entityEventMessage) {
        for (FlightOffline f : flightList) {
            if (f.flightState == FlightOffline.FLIGHT_STATE.CLOSED) {
                set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
                break;
            }
        }
    }
@Override
public void eventReceiver(EntityEventMessage entityEventMessage){
    ev = entityEventMessage.event;
    this.entityEventMessage = entityEventMessage;
    new FontLogAsync().execute(new EntityLogMessage(TAG,"eventReceiver:"+ev+":eventString:"+ entityEventMessage.eventMessageValueString, 'd'));
    switch(ev){
            case FLIGHT_REMOTENUMBER_RECEIVED:
                set_rAction(RACTION.ADD_OR_UPDATE_FLIGHT);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                setToNull();
                break;
            case CLOCK_MODECLOCK_ONLY:
                activeFlightControl = null;
                break;
            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                //if (routeNumber.equals(ROUTE_NUMBER_DEFAULT)) routeNumber = entityEventMessage.eventMessageValueString;
                break;
            case MACT_BIGBUTTON_ONCLICK_START:
                flightControlList.add(new FlightControl(ROUTE_NUMBER_DEFAULT,1));
                //set_rAction(RACTION.OPEN_NEW_FLIGHT);
                break;
            case FLIGHT_ONSPEEDLOW:
                //set_rAction(RACTION.RESTART_NEW_FLIGHT);
                restartNewFlight();
                break;
            case SESSION_ONSUCCESS_COMMAND:
                switch (entityEventMessage.eventMessageValueString) {
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        restartNewFlight();
                        break;
                }
                break;
        }
    }
}
