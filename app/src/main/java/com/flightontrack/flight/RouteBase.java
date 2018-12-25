package com.flightontrack.flight;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.model.EntityEventMessage;
import static com.flightontrack.definitions.EventEnums.*;

import java.util.ArrayList;

public class RouteBase implements EventBus{
    final String TAG = "RouteBase";

    public enum RACTION {
        OPEN_NEW_FLIGHT,
        //SWITCH_TO_PENDING,
        RESTART_NEW_FLIGHT,
        REMOVE_FLIGHT_IF_CLOSED,
        ADD_OR_UPDATE_FLIGHT

    }

    static RouteBase routeBaseInstance = null;
    public static Route activeRoute;
    public static FlightOnline activeFlight;
    public static FlightControl activeFlightControl;
    public static EntityFlightController activeEntityFlightController;
    public static ArrayList<FlightOffline> flightList = new ArrayList<>();
    public static ArrayList<FlightControl> flightControlList = new ArrayList<>();
    EntityEventMessage entityEventMessage;
    EVENT ev;

    public static RouteBase getInstance() {
        if(routeBaseInstance == null) {
            routeBaseInstance = new RouteBase();
        }
        return routeBaseInstance;
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
        RouteBase.activeFlight = null;
        RouteBase.activeRoute = null;
        EventBus.distribute(new EntityEventMessage(EVENT.ROUTE_FLIGHTLISTCLEAR));
    }
    void set_rAction(RACTION request) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "reaction:" + request, 'd'));
        switch (request) {
            case REMOVE_FLIGHT_IF_CLOSED:
                new FontLogAsync().execute(new EntityLogMessage(TAG, "CHECK_IF_CLOSED_FLIGHT: flightList: size : " + flightList.size(), 'd'));
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
                activeFlight = null;
                break;
        }
    }
}
