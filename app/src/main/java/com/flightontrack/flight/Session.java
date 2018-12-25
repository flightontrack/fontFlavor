package com.flightontrack.flight;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.ui.MainActivity;

//import static com.flightontrack.communication.SvcComm.commBatchSize;
import static com.flightontrack.flight.FlightOffline.*;
import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.definitions.Enums.*;
import static com.flightontrack.definitions.EventEnums.*;
import static com.flightontrack.flight.RouteBase.isFlightNumberInList;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

import com.flightontrack.communication.HttpJsonClient;
//import com.flightontrack.communication.SvcComm;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.model.EntityRequestPostLocation;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityLocation;
import com.flightontrack.mysql.SQLLocation;
import com.flightontrack.shared.EventBus;
import com.flightontrack.ui.ShowAlertClass;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
//import java.util.EnumMap;

public class Session implements EventBus{
    static final String TAG = "Session";

    public enum SACTION {
        SEND_CACHED_LOCATIONS,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
        GET_OFFLINE_FLIGHTS
    }
    static Session sessionInstance = null;
    static SQLLocation sqlLocation;
    public static Integer commBatchSize = COMM_BATCH_SIZE_MAX;
    //boolean isSendNextStarted = false;
    //static EnumMap<EVENT,SACTION> eventReaction = new EnumMap<>(EVENT.class);
    Map<Integer,EntityLocation> locRequestList = new HashMap<Integer,EntityLocation>();
    EVENT ev;

    EntityEventMessage entityEventMessage;
    public static Session getInstance() {
        if(sessionInstance == null) {
            sessionInstance = new Session();
            sqlLocation = SQLLocation.getInstance();
        }
        return sessionInstance;
    }

    public static void initProp(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences = ctx.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mainactivityInstance = maInstance;
//        eventReaction.put(MACT_BACKBUTTON_ONCLICK,SACTION.CHECK_CACHE_FIRST);
//        eventReaction.put(CLOCK_ONTICK,SACTION.START_COMMUNICATION);
//        eventReaction.put(ALERT_SENTPOINTS:
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.SEND_STORED_LOCATIONS);
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
//        eventReaction.put(ALERT_STOPAPP:
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
//        eventReaction.put(SETTINGACT_BUTTONSENDCACHE_CLICKED,SACTION.GET_OFFLINE_FLIGHTS);
//        eventReaction.put(FLIGHT_REMOTENUMBER_RECEIVED:


    }
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Boolean isNetworkAvailable = activeNetworkInfo != null&&activeNetworkInfo.isConnected();
        if (!isNetworkAvailable) Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
        return isNetworkAvailable ;
    }
    void addLocToRequestList(EntityLocation l){
        if (locRequestList.containsKey((int) l.itemId)) return;
        if (l.i >= commBatchSize) return;
        locRequestList.put((int) l.itemId, l);
    }
    void startLocationRequest() {
        ArrayList<EntityLocation> locList = sqlLocation.getAllLocationList();
        for (EntityLocation l : locList) {
            addLocToRequestList(l);
        }
//        if(!isSendNextStarted)  {
//            isSendNextStarted = true;
//            sendNext();
//        }
        sendNext();
    }
    void startLocationRequest(String flightNum) {

        ArrayList<EntityLocation> locList = sqlLocation.getFlightLocationList(flightNum);
        for (EntityLocation l : locList) {
            addLocToRequestList(l);
        }
//        if(!isSendNextStarted)  {
//            sendNext();
//        }
//        else {
//            //temp patch - in case of if isSendNextStarted did not reset
//            new FontLogAsync().execute(new EntityLogMessage(TAG, " isSendNextStarted : " + isSendNextStarted, 'd'));
//            if (locRequestList.entrySet().size() >5) sendNext();
//        }
        sendNext();
    }
    void sendNext(){

        if (locRequestList.isEmpty()) {
//            EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
            //isSendNextStarted = false;
            return;
        }
        for (Map.Entry<Integer, EntityLocation> e : locRequestList.entrySet()){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "Entrykey : " + e.getKey() + " Entryvalue : " + e.getValue(), 'd'));
        }
        Map.Entry<Integer, EntityLocation> e = locRequestList.entrySet().iterator().next();
        EntityLocation l = e.getValue();
        int k = e.getKey();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "Key : " + e.getKey()+ "Location : " + e.getValue(), 'd'));
        RequestParams requestParams = new RequestParams();
        requestParams.put("isdebug", SessionProp.pIsDebug);
        requestParams.put("speedlowflag", l.sl == 1);
        requestParams.put("rcode", l.rc);
        requestParams.put("latitude", l.la);
        requestParams.put("longitude", l.lo);
        requestParams.put("flightid", l.ft);
        requestParams.put("accuracy", l.ac);
        requestParams.put("extrainfo", l.al);
        requestParams.put("wpntnum", l.wp);
        requestParams.put("gsmsignal", l.sg);
        requestParams.put("speed", l.sd);
        requestParams.put("date", l.dt);
        requestParams.put("elevcheck", l.irch == 1);

        postLocation(k, l);
    }

    static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    void  set_Action(SACTION request) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "reaction:" + request, 'd'));
        switch (request) {
            case CHECK_CACHE_FIRST:
                if (dbLocationRecCountNormal > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCountNormal);
                    new FontLogAsync().execute(new EntityLogMessage(TAG, " PointsUnsent: " + dbLocationRecCountNormal, 'd'));
                } else {
                    set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                }
                break;
            case CLOSEAPP_NO_CACHE_CHECK:
                if(entityEventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) mainactivityInstance.finishActivity();
                break;
            case SEND_CACHED_LOCATIONS:
                    if (isNetworkAvailable()) {
                        startLocationRequest();
                    } else {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "Connectivity unavailable Can't send location", 'd'));
                        EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                    }
                break;
            case GET_OFFLINE_FLIGHTS:
                /// firsrt to check all temp flights in not ready to send state.
                /// Get new flight and request flight number.

                for (String flightNumTemp: sqlLocation.getTempFlightList()){
                    if(isFlightNumberInList(flightNumTemp)) {
                        /// flightOnline take care of the flight
                        continue;
                    }
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "Get flightOffline for " + flightNumTemp, 'd'));
                    if (isNetworkAvailable()) new FlightOffline(flightNumTemp).change_flightState(FLIGHT_STATE.GETTINGFLIGHT);
                    else {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "Connectivity unavailable Can't get flight number", 'd'));
                        EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                    }
                }

                /// second to check flights is ready to send which are for some reason not in flightList (may left from previous session).
                /// Get new flight on existing flight number

                //ArrayList<String> flightNumberList = sqlHelper.getReadyToSendFlightList();
//                for (String fn : sqlHelper.getReadyToSendFlightList()){
//                    if (isFlightNumberInList(fn)) continue;
//                    new FontLogAsync().execute(new EntityLogMessage(TAG,"Add flight "+fn+" to flightlisr",'d'));
//                    //new FlightOffline(fn).onFlightStateChanged(FlightOffline.FLIGHT_STATE.READY_TOSENDLOCATIONS);
//                    new FlightOffline(fn)
//                            .onFlightStateChanged(FLIGHT_STATE.STOPPED)
//                            .set_flightNumStatus(FlightOffline.FLIGHTNUMBER_SRC.REMOTE_DEFAULT);
//                }

//                List tempFlights = sqlHelper.getTempFlightList();
//                for (String flightNumTemp:sqlHelper.getAllFlightList()){
//                    if(isFlightNumberInList(flightNumTemp)) {
//                        /// flightOnline take care of the flight
//                        continue;
//                    }
//
//                    new FontLogAsync().execute(new EntityLogMessage(TAG, "Get flightOffline for " + flightNumTemp, 'd'));
//                    if (isNetworkAvailable()) new FlightOffline(flightNumTemp).onFlightStateChanged(FLIGHT_STATE.GETTINGFLIGHT);
//                    else {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "Connectivity unavailable Can't get flight number", 'd'));
//                        EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
//                    }
//                }

                break;


        }
    }

    void postLocation(int k, EntityLocation loc){
        if (isNetworkAvailable()) {
            try (
                    EntityRequestPostLocation entityRequestPostLocation = new EntityRequestPostLocation(loc);
                    HttpJsonClient client= new HttpJsonClient(entityRequestPostLocation)
            ){
                new FontLogAsync().execute(new EntityLogMessage(TAG,"Post: ID:"+loc.itemId, 'd'));
                client.post(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                        //new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess test k= " + k, 'd'));
                        if (commBatchSize == COMM_BATCH_SIZE_MIN) {
                            commBatchSize = COMM_BATCH_SIZE_MAX;
                        }

                        try (ResponseJsonObj response = new ResponseJsonObj(jsonObject)) {
                            if (response.isException) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess :isException", 'd'));
                                EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSUCCESS_EXCEPTION));
                                return;
                            }
                            if (response.responseAckn != null) {
                                sqlLocation.rowLocationDeleteOnId((int)loc.itemId, response.responseCurrentFlightNum);
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseCurrentFlightNum + ":" + response.responseAckn+ ": id" +response.responseAckn, 'd'));
                            }
                            if (response.responseException != null) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess :RESPONSE_TYPE_Exception :" + response.responseException, 'd'));
                                EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSUCCESS_EXCEPTION));
                            }
                            if (response.responseCommand != null) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess : RESPONSE_TYPE_COMMAND : " + response.responseCommand, 'd'));
                                if (response.responseCommand.equals(COMMAND_TERMINATEFLIGHT) && SessionProp.pIsRoad)
                                    return;
                                EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSUCCESS_COMMAND)
                                        .setEventMessageValueString(response.responseCommand));
                            }
                            locRequestList.remove(k);
                            EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
                            sendNext();
                        } catch (Exception e) {
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess : EXCEPTION :" + e.getMessage(), 'e'));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure; startId= " + response, 'd'));
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: "+e, 'd'));
                        locRequestList.clear();
                        commBatchSize = COMM_BATCH_SIZE_MIN;
                        //isSendNextStarted = false;
                        EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
                    }

                    @Override
                    public void onFinish() {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFinish For some reason is called before onSuccess key= " + k, 'd'));
                        //EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
                        //sendNext();
                    }
                });
            } catch (Exception e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "postLocation " + e.getMessage(), 'd'));
                return;
            }
        }

    }
    @Override
    public void onClock(EntityEventMessage entityEventMessage){
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onClock ", 'd'));
        if (dbLocationRecCountNormal > 0) set_Action(SACTION.SEND_CACHED_LOCATIONS);
        if (dbTempFlightRecCount>0) set_Action(SACTION.GET_OFFLINE_FLIGHTS);
    }

    @Override
    public void eventReceiver(EntityEventMessage entityEventMessage){
        //Array eventReaction[EVENT];
        ev = entityEventMessage.event;
        this.entityEventMessage = entityEventMessage;
        new FontLogAsync().execute(new EntityLogMessage(TAG, "eventReceiver: "+ev+":eventString:"+ entityEventMessage.eventMessageValueString, 'd'));
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_Action(SACTION.CHECK_CACHE_FIRST);
                break;
            case ALERT_SENTPOINTS:
                if(entityEventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.SEND_CACHED_LOCATIONS);
                if(entityEventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case ALERT_STOPAPP:
                if(entityEventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case SETTINGACT_BUTTONSENDCACHE_CLICKED:
                commBatchSize = COMM_BATCH_SIZE_MAX;
                //isSendNextStarted = false;
                if (sqlLocation.getLocationTableCountTotal() ==0){
                    EventBus.distribute(new EntityEventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
                    break;
                }
                else
                {
                    if (dbLocationRecCountNormal > 0) set_Action(SACTION.SEND_CACHED_LOCATIONS); /// only for the fligts in flightList already
                    set_Action(SACTION.GET_OFFLINE_FLIGHTS); /// check to see if any other flights need to be added to the flightList
                }
                break;
            case FLIGHT_REMOTENUMBER_RECEIVED:
                startLocationRequest(entityEventMessage.eventMessageValueString);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                set_Action(SACTION.SEND_CACHED_LOCATIONS);
                break;
        }
    }
}
