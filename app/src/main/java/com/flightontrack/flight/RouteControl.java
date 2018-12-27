package com.flightontrack.flight;

import com.flightontrack.definitions.Limits;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.Props;

import java.util.ArrayList;
import java.util.List;

import static com.flightontrack.definitions.EventEnums.EVENT;
import static com.flightontrack.definitions.Finals.COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED;
import static com.flightontrack.definitions.Finals.COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN;
import static com.flightontrack.definitions.Finals.ROUTE_NUMBER_DEFAULT;
import static com.flightontrack.flight.EntityFlightController.*;

public class RouteControl implements EventBus{
    final String TAG = "RouteControl";

    public enum RACTION {
        OPEN_NEW_FLIGHT,
        //SWITCH_TO_PENDING,
        RESTART_NEW_FLIGHT,
        CHECK_IF_CLOSED_FLIGHT,
        CHECK_IF_ADD_FLIGHT_TO_FLIGHTLIST

    }

    public static RouteControl routeControlInstance;
    public static FlightControl activeFlightControl;
    public static List<FlightControl> flightControlList;
    EntityEventMessage entityEventMessage;
    EVENT ev;

    public RouteControl(){
    }

    public static RouteControl getInstance() {
        if(routeControlInstance == null) {
            routeControlInstance = new RouteControl();
            flightControlList = new ArrayList<>();
        }
        return routeControlInstance;
    }

     public static FlightControl get_FlightInstanceByNumber(String flightNumber){
        for (FlightControl f : flightControlList) {
            if (f.flightNumber.equals(flightNumber)) {
                return f;
            }
        }
        return new FlightControl();
    }
    public boolean isFlightNumberInList(String flightNumber){
        for (FlightControl f : flightControlList) {
            if (f.flightNumber.equals(flightNumber)) {
                return true;
            }
        }
        return false;
    }

    void setToNull(){
        RouteControl.routeControlInstance = null;
        RouteControl.activeFlightControl = null;
        RouteControl.flightControlList = null;
    }

//    void set_rAction(RACTION request) {
//        new FontLogAsync().execute(new EntityLogMessage(TAG, "reaction:" + request, 'd'));
//        switch (request) {
////            case CHECK_IF_CLOSED_FLIGHT:
////                for (FlightControl f : new ArrayList<>(flightControlList)) {
////                    //new FontLogAsync().execute(new EntityLogMessage(TAG, "f:" + f.entityFlight.flightNumber + ":" + request, 'd'));
////                    if (f.flightState.equals(FLIGHT_STATE.CLOSED)) {
////                        new FontLogAsync().execute(new EntityLogMessage(TAG, "remove flight if closed: " +f.flightNumber, 'd'));
////                        if (f==activeFlightControl) activeFlightControl =null;
////                        /// remove from the list and database
////                        f.removeMyself();
////                    }
////                    if (flightControlList.isEmpty()) {
////                        //if (activeRoute == this) activeRoute = null;
////                        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightControlList isEmpty", 'd'));
////                        setToNull();
////                    }
////                }
////                break;
//////            case CHECK_IF_ADD_FLIGHT_TO_FLIGHTLIST:
////                FlightControl f = (FlightControl) entityEventMessage.eventMessageValueObject;
////                new FontLogAsync().execute(new EntityLogMessage(TAG, "flight added to flightlist "+f.flightNumber, 'd'));
////                if (flightControlList.contains(f)) break;
////                else {
////                    flightControlList.add(f);
////                    break;
////                }
//        }
//    }

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
        if (flightControlList.isEmpty()) {
            EventBus.distribute(new EntityEventMessage(EVENT.ROUTE_FLIGHTLIST_EMPTY));
            new FontLogAsync().execute(new EntityLogMessage(TAG, "onClock: flightControlList isEmpty", 'd'));
        }
    }
@Override
public void eventReceiver(EntityEventMessage entityEventMessage){
    ev = entityEventMessage.event;
    this.entityEventMessage = entityEventMessage;
    new FontLogAsync().execute(new EntityLogMessage(TAG,"eventReceiver:"+ev+":eventString:"+ entityEventMessage.eventMessageValueString, 'd'));
    switch(ev){
//            case FLIGHT_REMOTENUMBER_RECEIVED:
//                set_rAction(RACTION.CHECK_IF_ADD_FLIGHT_TO_FLIGHTLIST);
//                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
//                for (FlightControl f : new ArrayList<>(flightControlList)) {
//                    //new FontLogAsync().execute(new EntityLogMessage(TAG, "f:" + f.entityFlight.flightNumber + ":" + request, 'd'));
//                    if (f.flightState.equals(FLIGHT_STATE.CLOSED)) {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "remove flight if closed: " +f.flightNumber, 'd'));
//                        if (f==activeFlightControl) activeFlightControl =null;
//                        /// remove from the list and database
//                        f.removeMyself();
//                    }
//                }
                    if (flightControlList.isEmpty()) {
                        EventBus.distribute(new EntityEventMessage(EVENT.ROUTE_FLIGHTLIST_EMPTY));
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "FLIGHT_CLOSEFLIGHT_COMPLETED: flightControlList isEmpty", 'd'));
                    }
                break;
            case ROUTE_FLIGHTLIST_EMPTY:
                setToNull();
                break;
            case CLOCK_SERVICESELFSTOPPED:
                setToNull();
                break;
            case CLOCK_MODECLOCK_ONLY:
                activeFlightControl = null;
                break;
//            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
//                //if (routeNumber.equals(ROUTE_NUMBER_DEFAULT)) routeNumber = entityEventMessage.eventMessageValueString;
//                break;
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
