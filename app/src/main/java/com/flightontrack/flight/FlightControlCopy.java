package com.flightontrack.flight;

import android.content.ContentValues;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.clock.SvcLocationClock;
import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.definitions.Limits;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.model.EntityFlight;
import com.flightontrack.model.EntityFlightTimeMessage;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityRequestCloseFlight;
import com.flightontrack.model.EntityRequestNewFlight;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.objects.Aircraft;
import com.flightontrack.objects.MyDateTime;
import com.flightontrack.objects.MyPhone;
import com.flightontrack.objects.Pilot;
import com.flightontrack.shared.EventBus;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import other.Talk;

import static com.flightontrack.definitions.EventEnums.EVENT;
import static com.flightontrack.definitions.Finals.COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED;
import static com.flightontrack.definitions.Finals.COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN;
import static com.flightontrack.definitions.Finals.COMMAND_TERMINATEFLIGHT;
import static com.flightontrack.definitions.Finals.DEFAULT_TIME_BW_GPS_UPDATES_SEC;
import static com.flightontrack.definitions.Finals.ELEVATIONCHECK_FLIGHT_TIME_SEC;
import static com.flightontrack.definitions.Finals.FLIGHT_NUMBER_DEFAULT;
import static com.flightontrack.definitions.Finals.REQUEST_LOCATION_UPDATE;
import static com.flightontrack.definitions.Finals.ROUTE_NUMBER_DEFAULT;
import static com.flightontrack.definitions.Finals.SPEEDLOW_TIME_BW_GPS_UPDATES_SEC;
import static com.flightontrack.flight.EntityFlightController.FLIGHTNUMBER_SRC;
import static com.flightontrack.flight.EntityFlightController.FLIGHTNUMBER_SRC.REMOTE_DEFAULT;
import static com.flightontrack.flight.EntityFlightController.FLIGHT_STATE;
import static com.flightontrack.flight.EntityFlightController.FLIGHT_STATE.READY_TOSAVELOCATIONS;
import static com.flightontrack.flight.Session.isNetworkAvailable;
import static com.flightontrack.shared.Props.SessionProp;
import static com.flightontrack.shared.Props.SessionProp.sqlLocation;
import static com.flightontrack.shared.Props.mainactivityInstance;

public class FlightControlCopy implements EventBus {
    static final String TAG = "FlightControl";

    EntityFlightController entityFlightController;
    EntityFlight entityFlight;
    private List<Integer>   dbIdList = new ArrayList<>();
    private MyDateTime myDateTime;
    private boolean         isGettingFlight = false;
    public int              lastAltitudeFt;
    private float           speedCurrent = 0;
    private float           speedPrev = 0;
    private int             talkCount;
    private boolean         isElevationCheckDone = false;
    boolean isSpeedAboveMin = false;

    public FlightControlCopy(EntityFlightController fc) {
        entityFlightController = fc;
        RouteBase.activeFlightControl = this;
        myDateTime = new MyDateTime();
        entityFlight = new EntityFlight(fc.routeNumber, myDateTime.dateLocal,new Aircraft().AcftNum);
    }

    void checkWayPointsCount() {
        //entityFlight.wayPointsCount = pointsCount;
        if (entityFlight.wayPointsCount >= Limits.getWayPointLimit()) {
            EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_ONPOINTSLIMITREACHED));
        }
    }

    void set_speedCurrent(float speed) {
        /// gps can report speed equal 0 in flight  which should be ignored.
        if ((speed > 0.0) | SessionProp.pIsDebug) {
            speedPrev = speedCurrent;
            /// this 0.1 is needed to start flight whe Flight min speed set to 0;
            speedCurrent = speed + (float) 0.01;
        } else {
            /// this condition never happen when writing a log file because SessionProp.pIsDebug == true
            // new FontLogAsync().execute(new LogMessage(TAG, "set_speedCurrent: Reported speed is ZERO", 'd');
        }
        // new FontLogAsync().execute(new LogMessage(TAG, "set_speedCurrent: " + speedCurrent, 'd');
    }


    boolean is_DoubleSpeedAboveMin() {
        double cutoffSpeed = get_cutoffSpeed();
        boolean isCurrSpeedAboveMin = (speedCurrent >= cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev >= cutoffSpeed);
        //new FontLogAsync().execute(new LogMessage(TAG, "is_DoubleSpeedAboveMin: cutoffSpeed: " + cutoffSpeed, 'd');
        if (isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
            //else if (RouteBase.activeFlight.lastAction == FACTION.CHANGE_IN_FLIGHT && (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin)) {
        else if (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin + " isPrevSpeedAboveMin:" + isPrevSpeedAboveMin, 'd'));
            int intervalClockSec = DEFAULT_TIME_BW_GPS_UPDATES_SEC;
            if (isPrevSpeedAboveMin) intervalClockSec = SPEEDLOW_TIME_BW_GPS_UPDATES_SEC;
                        //EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDCHANGE).setEventMessageValueInt(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC));
                //SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin) intervalClockSec = SvcLocationClock.intervalClockSecPrev;
                //SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_ONSPEEDCHANGE).setEventMessageValueInt(intervalClockSec));
            return true;
        }
        return false;
    }

    void get_NewFlightID() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, ".....-get_NewFlightID", 'd'));
        isGettingFlight = true;
        try(
            Aircraft aircraft = new Aircraft();
            EntityRequestNewFlight entityRequestNewFlight = new EntityRequestNewFlight()
            .set("phonenumber", MyPhone.myPhoneId)
            .set("username", Pilot.getPilotUserName())
            .set("userid", Pilot.getUserID())
            .set("deviceid", MyPhone.myDeviceId)
            .set("aid", MyPhone.getMyAndroidID())
            .set("versioncode", String.valueOf(MyPhone.getVersionCode()))
            .set("AcftNum", aircraft.AcftNum)
            .set("AcftTagId", aircraft.AcftTagId)
            .set("AcftName", aircraft.AcftName)
            .set("isFlyingPattern", String.valueOf(SessionProp.pIsMultileg))
            .set("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec))
            .set("speed_thresh", String.valueOf(Math.round(SessionProp.pSpinnerMinSpeed)))
            .set("isdebug", String.valueOf(SessionProp.pIsDebug))
            .set("routeid", entityFlightController.routeNumber.equals(ROUTE_NUMBER_DEFAULT)?null:entityFlightController.routeNumber);

            HttpJsonClient client = new HttpJsonClient(entityRequestNewFlight)
            )
        {
            client.post(new JsonHttpResponseHandler() {

                @Override
                public void onStart() {
                    Log.i(TAG, "oonStart " + client.urlLink   );
                }

                @Override
                public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                    //Log.i("TAG", "onSuccessjsonObject: " + jsonObject);
                    ResponseJsonObj response = new ResponseJsonObj(jsonObject);
                    if (response.responseException != null) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "RESPONSE_TYPE_NOTIF: " + response.responseException, 'd'));
                        Toast.makeText(mainactivityInstance, R.string.cloud_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.responseNewFlightNum != null) {

                        if (entityFlightController.flightNumStatus == FLIGHTNUMBER_SRC.LOCAL){
                            /// in case of request resubmitting for flight number on this flight
                            entityFlightController.setFlightNumStatus(FLIGHTNUMBER_SRC.REMOTE_DEFAULT);
                        }
                        else {
                            /// normal flow
                            //entityFlightController.isGetFlightCallSuccess = true;
                            //route._legCount++;
                        }
                        entityFlightController.setFlightNumber(response.responseNewFlightNum);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: " + e.getMessage(), 'd'));
                    //client.isFailed = true;
                    if (entityFlightController.flightNumStatus == REMOTE_DEFAULT) if (mainactivityInstance != null) {
                        Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
                        EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                .setEventMessageValueBool(false)
                                .setEventMessageValueString(FLIGHT_NUMBER_DEFAULT));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure URL method not found status code: " + statusCode, 'd'));
                    //client.isFailed = true;
                    if (entityFlightController.flightNumStatus == REMOTE_DEFAULT) if (mainactivityInstance != null) {
                        Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
                        EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                .setEventMessageValueBool(false)
                                .setEventMessageValueString(FLIGHT_NUMBER_DEFAULT));
                    }
                }

                @Override
                public void onRetry(int retryNo) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "get_NewFlightID onRetry:" + retryNo, 'd'));
                }
                @Override
                public void onFinish() {
                    isGettingFlight = false;
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "get_NewFlightID onFinish", 'd'));
                }
              });
            }
            catch(Exception e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: ", 'e'));
            }
        }

        void get_CloseFlight() {
        String TAG = "getCloseFlightNew";
        new FontLogAsync().execute(new EntityLogMessage(TAG, "get_CloseFlight: " + entityFlight.flightNumber, 'd'));
        entityFlightController.setFlightState(FLIGHT_STATE.CLOSING);
        //change_flightState(FLIGHT_STATE.CLOSING);
        EntityRequestCloseFlight entityRequestCloseFlight = new EntityRequestCloseFlight()
                .set("flightid", entityFlight.flightNumber)
                .set("isdebug", SessionProp.pIsDebug)
                .set("speedlowflag", !isSpeedAboveMin)
                .set("isLimitReached", entityFlightController.isLimitReached);
        //.set("isJunkFlight", isJunkFlight);
        try (
                HttpJsonClient client= new HttpJsonClient(entityRequestCloseFlight)
        )
        {
            client.post(new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "get_CloseFlight OnSuccess", 'd'));
                                ResponseJsonObj response = new ResponseJsonObj(jsonObject);

                                if (response.responseAckn != null) {
                                    new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess|Flight closed: " + entityFlight.flightNumber, 'd'));
                                }
                                if (response.responseException != null) {
                                    new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess|RESPONSE_TYPE_NOTIF:" + response.responseException, 'd'));
                                }
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "get_CloseFlight onFailure: " + entityFlight.flightNumber, 'd'));
                            }

                            @Override
                            public void onFinish() {
                                entityFlightController.setFlightState(FLIGHT_STATE.CLOSED);
                                //change_flightState(FLIGHT_STATE.CLOSED);
                            }
                        }
            );
        }
        catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "get_CloseFlight" + e.getMessage(), 'e'));
        }
    }
    public void saveLocCheckSpeed(final Location location) {

        float speedCurrent = location.getSpeed();
        //new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocCheckSpeed: reported speed: " + speedCurrent, 'd'));
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = is_DoubleSpeedAboveMin();
        switch (entityFlightController.flightState) {
            case READY_TOSAVELOCATIONS:
                if (isSpeedAboveMin) entityFlightController.setFlightState(FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN); //onFlightStateChanged(FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN);
                break;

            case INFLIGHT_SPEEDABOVEMIN:
                if (!isElevationCheckDone) {
                    if (entityFlight.flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                set_flightTimeSec();
                if (!isSpeedAboveMin) {
                    entityFlightController.setFlightState(FLIGHT_STATE.STOPPED); //onFlightStateChanged(FLIGHT_STATE.STOPPED);
                    EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_ONSPEEDLOW).setEventMessageValueString(entityFlight.flightNumber));
                }
                break;
        }
    }

    private void saveLocation(Location location, boolean iselevecheck) {
        try {
            int p = entityFlight.wayPointsCount + 1;
            ContentValues values = new ContentValues();
            values.put(DBSchema.COLUMN_NAME_COL1, REQUEST_LOCATION_UPDATE); //rcode
            values.put(DBSchema.LOC_flightid, entityFlight.flightNumber); //flightid
            values.put(DBSchema.LOC_isTempFlight, entityFlightController.flightNumStatus == FLIGHTNUMBER_SRC.LOCAL); //istempflightnum
            values.put(DBSchema.LOC_speedlowflag, !isSpeedAboveMin); /// speed low
            //values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString(speedCurrentInt)); //speed
            values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString((int) location.getSpeed())); //speed
            values.put(DBSchema.COLUMN_NAME_COL6, Double.toString(location.getLatitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL7, Double.toString(location.getLongitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL8, Float.toString(location.getAccuracy())); //accuracy
            values.put(DBSchema.COLUMN_NAME_COL9, Math.round(location.getAltitude())); //extrainfo
            values.put(DBSchema.LOC_wpntnum, p); //wpntnum
            values.put(DBSchema.COLUMN_NAME_COL11, Integer.toString(Pilot.getSignalStregth())); //gsmsignal
            values.put(DBSchema.LOC_date, URLEncoder.encode(myDateTime.updateDateTime().dateTimeLocalString, "UTF-8")); //date
            values.put(DBSchema.LOC_is_elevetion_check, iselevecheck);
            long r = sqlLocation.insertRowLocation(values);
            if (r > 0) {
                dbIdList.add((int) r);
                lastAltitudeFt = (int) (Math.round(location.getAltitude() * 3.281));
                entityFlight.wayPointsCount = p;
                checkWayPointsCount();
                new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocation: dbLocationRecCountNormal: " + SessionProp.dbLocationRecCountNormal, 'd'));
            }
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "SQLite Exception Placeholder", 'e'));
        }
    }

    public void set_flightTimeSec() {
        //long elapsedTime = getTime.getTimeGMT()- entityFlight.flightTimeStartGMT;
        entityFlight.setFlightTime(myDateTime.getTimeGMT()- entityFlight.flightTimeStartGMT);
        entityFlight.setFlightDuration(myDateTime.getElapsedTimeString(entityFlight.flightTime));
        //flightTimeSec = getTime.elapsedTimeSec;
        new FontLogAsync().execute(new EntityLogMessage(TAG,"flightTimeSec =  "+entityFlight.flightTimeSec, 'd'));
//        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
//        flightTimeString = dateFormat.format(timeDiff);
        EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_FLIGHTTIME_UPDATE_COMPLETED).setEventMessageValueString(entityFlight.flightNumber));
        //int tc = flightTimeSec/60/TIME_TALK_INTERVAL_MIN;
        //new FontLogAsync().execute(new EntityLogMessage(TAG,"c =  "+c, 'd'));
        //new FontLogAsync().execute(new EntityLogMessage(TAG,"talkCount =  "+talkCount, 'd'));
        if (entityFlight.talkTime>talkCount){
            talkCount=entityFlight.talkTime;
            new Talk(new EntityFlightTimeMessage(entityFlight.flightTimeSec));
        }
    }

    double get_cutoffSpeed() {
        //return SessionProp.pSpinnerMinSpeed * (RouteBase.activeFlight.flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN ? 0.75 : 1.0);
        return SessionProp.pSpinnerMinSpeed * (entityFlightController.flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN ? 0.75 : 1.0);
    }

    public FlightControlCopy onFlightStateChanged() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFlightStateChanged: change to: "+ entityFlightController.flightState, 'd'));
        switch (entityFlightController.flightState) {
            case GETTINGFLIGHT:
                EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
                get_NewFlightID();
                break;
            case READY_TOSAVELOCATIONS:
                EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_STATECHANGEDTO_READYTOSAVE).setEventMessageValueString(entityFlight.flightNumber));
                break;
            case INFLIGHT_SPEEDABOVEMIN:
                entityFlight.setFlightTimeStartGMT(myDateTime.updateDateTime().dateTimeGMT);
                entityFlight.setFlightTimeStart(myDateTime.timeLocal);
                //entityFlightController.setIsJunk(0);
                //getTime = new GetTime();
                //flightStartTimeGMT = getTime.initDateTimeGMT;
                //entityFlight = new EntityFlight(flightNumber,route.routeNumber,getTime.dateLocal,getTime.timeLocal,new Aircraft().AcftNum);
                EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_ONSPEEDABOVEMIN).setEventMessageValueString(entityFlight.flightNumber));
                //entityFlight.dbid= sqlHelper.insertFlightEntityRecord(entityFlight);
                break;
            case STOPPED:
                if (sqlLocation.getLocationFlightCount(entityFlight.flightNumber) == 0) {
                    entityFlightController.setFlightState(FLIGHT_STATE.READY_TOBECLOSED);
                }
                break;
            case READY_TOBECLOSED:
                //List<EntityFlight> l = sqlHelper.getFlightHistList();
                //new FontLogAsync().execute(new EntityLogMessage(TAG, "history list size: "+l.size(), 'd'));
                get_CloseFlight();
                break;
            case CLOSING:
                break;
            case CLOSED:
                EventBus.distribute(new EntityEventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(entityFlight.flightNumber));
                break;
//            default:
//                super.change_flightState(fs);
//                break;
        }
        return this;
    }

//    public void set_flightState(FLIGHT_STATE fs, String descr) {
//        onFlightStateChanged(fs);
//        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightState reasoning : " + fs + ' ' + descr, 'd'));
//    }

    public String getFlightTime(){
        return entityFlight.flightDuration;
    }

    @Override
    public void onClock(EntityEventMessage entityEventMessage) {

        new FontLogAsync().execute(new EntityLogMessage(TAG, "onClock "+entityFlight.flightNumber+" afs:"+entityFlightController.flightState+" loccount:"+ sqlLocation.getLocationFlightCount(entityFlight.flightNumber), 'd'));
        //if (RouteBase.activeFlight == this
        if (RouteBase.activeEntityFlightController == this.entityFlightController
                && (entityFlightController.flightState == READY_TOSAVELOCATIONS || entityFlightController.flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN)
                && entityEventMessage.eventMessageValueLocation != null) {
            //                    String s = Arrays.toString(Thread.currentThread().getStackTrace());
            //                    new FontLogAsync().execute(new LogMessage(TAG, "StackTrace: "+s,'d');
            saveLocCheckSpeed(entityEventMessage.eventMessageValueLocation);
        }
        if (isNetworkAvailable() && !isGettingFlight) {
            if (entityFlightController.flightNumStatus == FLIGHTNUMBER_SRC.LOCAL) get_NewFlightID();
        }
        if (entityFlightController.flightState == FLIGHT_STATE.STOPPED && sqlLocation.getLocationFlightCount(entityFlight.flightNumber) == 0) {
            entityFlightController.setFlightState(FLIGHT_STATE.READY_TOBECLOSED); //onFlightStateChanged(FLIGHT_STATE.READY_TOBECLOSED);
        }
    }

    @Override
    public void eventReceiver(EntityEventMessage entityEventMessage) {
        EVENT ev = entityEventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, entityFlight.flightNumber + ":eventReceiver:" + ev, 'd'));
        switch (ev) {
            case SESSION_ONSUCCESS_COMMAND:
                String server_command = entityEventMessage.eventMessageValueString;
                new FontLogAsync().execute(new EntityLogMessage(TAG, "server_command int: " + server_command, 'd'));
                switch (server_command) {
                    case COMMAND_TERMINATEFLIGHT:
                        //isJunkFlight = true;
                        Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
                        entityFlight.setIsJunk(1);
                        entityFlightController.setIsJunk(1);
                        entityFlightController.setFlightState(FLIGHT_STATE.STOPPED, "Terminate flight server command");
                        //set_flightState(FLIGHT_STATE.STOPPED, "Terminate flight server command");
                        //set_fAction(FACTION.TERMINATE_FLIGHT);
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        /// this server request is disabled for now
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        entityFlightController.isLimitReached = true;
                        entityFlightController.setFlightState(FLIGHT_STATE.STOPPED); //                     onFlightStateChanged(FLIGHT_STATE.STOPPED);
                        break;
                }
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                if (entityFlightController.flightState == FLIGHT_STATE.GETTINGFLIGHT) entityFlightController.setFlightState(FLIGHT_STATE.CLOSED); //onFlightStateChanged(FLIGHT_STATE.CLOSED);
                else entityFlightController.setFlightState(FLIGHT_STATE.STOPPED); //onFlightStateChanged(FLIGHT_STATE.STOPPED);
                //TODO remove flight points
                break;
            case SQL_LOCALFLIGHTNUM_ALLOCATED:
                entityFlightController.setFlightNumStatus(FLIGHTNUMBER_SRC.LOCAL);
                entityFlightController.setFlightNumber(entityEventMessage.eventMessageValueString);
                //isGetFlightCallSuccess = true;
                //route._legCount++;
                //onFlightStateChanged(FLIGHT_STATE.READY_TOSAVELOCATIONS);
                break;
            case SQL_FLIGHTRECORDCOUNT_ZERO:
                if (entityFlightController.flightState == FLIGHT_STATE.STOPPED) entityFlightController.setFlightState(FLIGHT_STATE.READY_TOBECLOSED); //change_flightState(FLIGHT_STATE.READY_TOBECLOSED);
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                //entityFlight.flightDuration = flightTimeString;
                break;
        }
    }
}
