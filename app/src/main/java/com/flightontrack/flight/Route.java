package com.flightontrack.flight;

import com.flightontrack.definitions.Limits;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.Props;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.definitions.EventEnums.*;

public class Route extends RouteBase implements EventBus{
    private final String TAG = "Route";

    //int _legCount = 0;
    String routeNumber=ROUTE_NUMBER_DEFAULT;

    public Route() {
        activeRoute = this;
    }

//    void set_rAction(RACTION request) {
//        //new FontLogAsync().execute(new LogMessage(TAG, "reaction:" + request, 'd');
//        switch (request) {
//            case OPEN_NEW_FLIGHT:
//                /// starting new flight
//                flightList.add(new FlightOnline(this));
//                break;
////            case SWITCH_TO_PENDING:
////                break;
//            case RESTART_NEW_FLIGHT:
//                if (Props.SessionProp.pIsMultileg && (_legCount < Limits.LEG_COUNT_HARD_LIMIT)) {
//                    /// ignore request to close route
//                    flightList.add(new FlightOnline(this));
//                } else {
//                    EventBus.distribute(new EntityEventMessage(EVENT.ROUTE_ONRESTART).setEventMessageValueBool(false));
//                }
//                break;
////            case CLOSE_RECEIVEFLIGHT_FAILED:
////                flightList.remove(flightList.size() - 1);  /// remove the latest flight added
////                if (!(SvcLocationClock.instanceSvcLocationClock == null))
////                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
////                break;
////            case RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER:
//////                _legCount++;
//////                int tempFlight = sqlHelper.getNewTempFlightNum();
//////
//////
//////                if (!(SvcLocationClock.instanceSvcLocationClock == null))
//////                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
//////                    //set_routeStatus(RSTATUS.PASSIVE);
//////                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
//////                    //activeRoute =null;
////                break;
//        }
//    }

//    static void checkIfAnyFlightNeedClose() {
//        try {
//            for (Route r : Route.routeList) {
//                for (Flight f : r.flightList) {
//                    if (f.lastAction == FACTION.CHANGE_IN_WAIT_TO_CLOSEFLIGHT) {
//                        f.set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
//                    }
//                    //String flights ="-";
//                    //flights=flights+f.flightNumber+"-"+f.lastAction+"-";
//                }
//            }
//        } catch (Exception e) {
//            //new FontLogAsync().execute(new LogMessage(TAG, "checkIfAnyFlightNeedClose: " + e.getMessage() + "\n" + e.getCause(), 'e');
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
public void eventReceiver(EntityEventMessage entityEventMessage){
    EVENT ev = entityEventMessage.event;
    new FontLogAsync().execute(new EntityLogMessage(TAG, routeNumber+" :eventReceiver:"+ev+":eventString:"+ entityEventMessage.eventMessageValueString, 'd'));
    switch(ev){
                case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                    if (routeNumber.equals(ROUTE_NUMBER_DEFAULT)) routeNumber = entityEventMessage.eventMessageValueString;
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
