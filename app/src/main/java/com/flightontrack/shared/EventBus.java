package com.flightontrack.shared;
import com.flightontrack.control.FlightControl;
import com.flightontrack.control.RouteControl;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.mysql.SQLLocation;
import com.flightontrack.ui.BigButton;
import com.flightontrack.ui.SimpleSettingsActivity;
import com.flightontrack.control.Session;
import com.flightontrack.clock.SvcLocationClock;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;

import java.util.ArrayList;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.definitions.EventEnums.*;

public interface EventBus{
    String TAG = "Bus";

    static void distribute(EntityEventMessage entityEventMessage){
        ArrayList<EventBus> interfaceList = new ArrayList();
        ArrayList<EventBus> onClockList = new ArrayList();

        EVENT ev = entityEventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, ev+":eventString:"+ entityEventMessage.eventMessageValueString+":eventObject:"+ entityEventMessage.eventMessageValueObject, 'd'));
        switch(ev){
            case HEALTHCHECK_ONRESTART:
                interfaceList.add(mainactivityInstance);
                break;
            case MACT_BIGBUTTON_ONCLICK_START:
                interfaceList.add(RouteControl.getInstance());
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                interfaceList.add(Props.getInstance());
                interfaceList.add(RouteControl.activeFlightControl); // close flight of set the pending flight to fail
                //interfaceList.add(Session.getInstance());
                interfaceList.add(SvcLocationClock.getInstance());
                break;
//            case PROP_CHANGED_MULTILEG:
//                interfaceList.add(mainactivityInstance);
//                break;
            case FLIGHT_GETNEWFLIGHT_STARTED:
                //interfaceList.add(mainactivityInstance);
                interfaceList.add(BigButton.getInstance());
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if(!entityEventMessage.eventMessageValueBool){
                    interfaceList.add(SQLLocation.getInstance());
                }
                break;
            case FLIGHTBASE_GETFLIGHTNUM:
                if(!entityEventMessage.eventMessageValueBool){
                    interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                }
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                interfaceList.add(mainactivityInstance);
                interfaceList.add(BigButton.getInstance());
                interfaceList.add(RouteControl.activeFlightControl);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                interfaceList.add((FlightControl) entityEventMessage.eventMessageValueObject); ///self remove flight fom list
                interfaceList.add(RouteControl.getInstance()); /// check if list is empty
                break;
            case FLIGHT_ONSPEEDLOW:
                if(!SessionProp.pIsMultileg) interfaceList.add(SvcLocationClock.getInstance());//TODO doing nothing
                interfaceList.add(RouteControl.getInstance()); /// restart new flight in the route
                break;
            case FLIGHT_ONSPEEDCHANGE:
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case FLIGHT_ONSPEEDABOVEMIN:
                interfaceList.add(SvcLocationClock.getInstance()); ///
                break;
            case FLIGHT_ONPOINTSLIMITREACHED:
                ///TODO
                break;
            case ROUTE_ONLEGLIMITREACHED:
                ///TODO
                break;
            case ROUTE_FLIGHTLIST_EMPTY:
                interfaceList.add(SvcLocationClock.getInstance());
                //else interfaceList.add(BigButton.getInstance()); //interfaceList.add(mainactivityInstance);
                interfaceList.add(RouteControl.getInstance());
                break;
            case ROUTE_ONRESTART:
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case SESSION_ONSUCCESS_EXCEPTION:
                interfaceList.add(Props.getInstance());
                interfaceList.add(SvcLocationClock.getInstance());
                interfaceList.add(mainactivityInstance);
                break;
            case SESSION_ONSUCCESS_COMMAND:
                //interfaceList.add(Route.get_FlightInstanceByNumber(eventMessage.eventMessageValueString));
                switch (entityEventMessage.eventMessageValueString){
                    case COMMAND_TERMINATEFLIGHT_ON_ALTITUDE:
                        interfaceList.add(Props.getInstance()); //set multileg to false
                        //interfaceList.add(sqlHelper); // delete all locations on the flight
                        interfaceList.add((FlightControl) entityEventMessage.eventMessageValueObject); // stop active flight
                        if(RouteControl.activeFlightControl==entityEventMessage.eventMessageValueObject){
                            interfaceList.add(SvcLocationClock.getInstance());} //swithch to clockonly
                        break;
                    case COMMAND_TERMINATEFLIGHT_SPEED_BELOW_MIN:
                        interfaceList.add(RouteControl.getInstance()); //initiate a new flight if multileg
                        break;
                    case COMMAND_TERMINATEFLIGHT_ON_LIMIT_REACHED:
                        interfaceList.add(RouteControl.get_FlightInstanceByNumber(entityEventMessage.eventMessageValueFlightNumString)); // stop active flight
                        interfaceList.add(RouteControl.getInstance()); //initiate a new flight if multileg
                        break;
                }
            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:
                interfaceList.add(SQLLocation.getInstance());
                break;
            case SETTINGACT_BUTTONSENDCACHE_CLICKED:
                interfaceList.add(new SvcLocationClock());
                interfaceList.add(Session.getInstance());
                break;
            case MACT_MULTILEG_ONCLICK:
                interfaceList.add(Props.getInstance());
                break;
            case CLOCK_MODECLOCK_ONLY:
                //interfaceList.add(mainactivityInstance);
                interfaceList.add(BigButton.getInstance());
                interfaceList.add(RouteControl.getInstance());
                break;
            case CLOCK_SERVICESELFSTOPPED:
                interfaceList.add(RouteControl.getInstance());
                interfaceList.add(Session.getInstance());
                break;
            case CLOCK_ONTICK:
                onClockList.add(RouteControl.getInstance()); /// delete closed flights from flightlist
//                for (FlightOffline f : Route.flightList) {
//                    onClockList.add(f);                   /// check if any of them need to be replace temp flight num                     /// check if any of them need to be closed
//                }
                onClockList.addAll(RouteControl.flightControlList);
                onClockList.add(Session.getInstance());   /// start communication service
                break;
            case ALERT_SENTPOINTS:
                interfaceList.add(RouteControl.getInstance());
                interfaceList.add(Session.getInstance());
                break;
            case MACT_BACKBUTTON_ONCLICK:
                interfaceList.add(Session.getInstance());
                break;
            case ALERT_STOPAPP:
                interfaceList.add(Session.getInstance());
                break;
            case SQL_LOCALFLIGHTNUM_ALLOCATED:
                interfaceList.add(RouteControl.activeFlightControl);
                break;
            case SQL_ONCLEARCACHE_COMPLETED:
                interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                break;
            case SQL_FLIGHTRECORDCOUNT_ZERO:
                interfaceList.add((FlightControl) entityEventMessage.eventMessageValueObject);
                break;
            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                //interfaceList.add(RouteControl.getInstance()); // set route number
                interfaceList.add(new SvcLocationClock()); //start clock service in location mode
                //interfaceList.add(mainactivityInstance);
                interfaceList.add(BigButton.getInstance());
                break;
            case FLIGHT_REMOTENUMBER_RECEIVED:
                //interfaceList.add(RouteControl.getInstance()); /// add the offline flights to flightlist if it is not in
                interfaceList.add(Session.getInstance());   /// start send locations for the flights with replaced flight number
                break;
            case SESSION_ONSENDCACHECOMPLETED:
                if (SimpleSettingsActivity.simpleSettingsActivityInstance!=null) interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                interfaceList.add(mainactivityInstance);
                //interfaceList.add(Session.getInstance()); /// if still location left send
                break;
        }
        for( EventBus i : interfaceList) {
            if(!(null==i))i.eventReceiver(entityEventMessage);
            else  new FontLogAsync().execute(new EntityLogMessage(TAG, " null interface ", 'd'));
        }
        for( EventBus i : onClockList) {
            if(!(null==i))i.onClock(entityEventMessage);
            else  new FontLogAsync().execute(new EntityLogMessage(TAG, " null interface ", 'd'));
        }
    }

    default void eventReceiver(EntityEventMessage entityEventMessage){}
    default void onClock(EntityEventMessage entityEventMessage){}
}


