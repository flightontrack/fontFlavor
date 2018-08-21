package com.flightontrack.flight;

import android.content.ContentValues;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.entities.EntityFlightTimeMessage;
import com.flightontrack.entities.EntityRequestNewFlight;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.other.TalkAsync;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.GetTime;
import com.flightontrack.shared.Props;
import com.flightontrack.shared.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.flight.FlightOffline.FLIGHTNUMBER_SRC.REMOTE_DEFAULT;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class FlightOnline extends FlightOffline implements GetTime, EventBus {
    static final String TAG = "FlightOnline";

    public String flightTimeString;
    public int lastAltitudeFt;
    public int _wayPointsCount;
    Route route;
    float _speedCurrent = 0;
    float speedPrev = 0;
    int _flightTimeSec;
    private long _flightStartTimeGMT;
    boolean isElevationCheckDone;
    double cutoffSpeed;
    boolean isGettingFlight = false;
    boolean isGetFlightCallSuccess = false;
    List<Integer> dbIdList = new ArrayList<>();

    public FlightOnline(Route r) {
        route = r;
        flightTimeString = FLIGHT_TIME_ZERO;
        isElevationCheckDone = false;
        RouteBase.activeFlight = this;
        set_flightState(FLIGHT_STATE.GETTINGFLIGHT);
    }

    public void set_flightNumber(String fn) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, " set_flightNumber " + fn + " flightNumStatus " + flightNumStatus, 'd'));
        replaceFlightNumber(fn);
        switch (flightNumStatus) {
            case REMOTE_DEFAULT:
                set_flightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
                break;
            case LOCAL:
                set_flightNumStatus(REMOTE_DEFAULT);
                break;
        }
    }

    void set_wayPointsCount(int pointsCount) {
        _wayPointsCount = pointsCount;
        if (pointsCount >= Util.getWayPointLimit()) {
            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONPOINTSLIMITREACHED));
        }
    }

    void set_speedCurrent(float speed) {
        /// gps can report speed equal 0 in flight  which should be ignored.
        if ((speed > 0.0) | SessionProp.pIsDebug) {
            speedPrev = _speedCurrent;
            /// this 0.1 is needed to start flight whe Flight min speed set to 0;
            _speedCurrent = speed + (float) 0.01;
        } else {
            /// this condition never happen when writing a log file because SessionProp.pIsDebug == true
            // new FontLogAsync().execute(new LogMessage(TAG, "set_speedCurrent: Reported speed is ZERO", 'd');
        }
        // new FontLogAsync().execute(new LogMessage(TAG, "set_speedCurrent: " + _speedCurrent, 'd');
    }


    boolean isDoubleSpeedAboveMin() {
        cutoffSpeed = get_cutoffSpeed();
        boolean isCurrSpeedAboveMin = (_speedCurrent >= cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev >= cutoffSpeed);
        //new FontLogAsync().execute(new LogMessage(TAG, "isDoubleSpeedAboveMin: cutoffSpeed: " + cutoffSpeed, 'd');
        if (isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
            //else if (RouteBase.activeFlight.lastAction == FACTION.CHANGE_IN_FLIGHT && (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin)) {
        else if (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin + " isPrevSpeedAboveMin:" + isPrevSpeedAboveMin, 'd'));
            if (isPrevSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            return true;
        }
        return false;
    }

    void getNewFlightID() {
        isGettingFlight = true;
            EntityRequestNewFlight entityRequestNewFlight = new EntityRequestNewFlight()
            .set("phonenumber", MyPhone._myPhoneId)
            .set("username", Pilot.getPilotUserName())
            .set("userid", Pilot.getUserID())
            .set("deviceid", MyPhone._myDeviceId)
            .set("aid", MyPhone.getMyAndroidID())
            .set("versioncode", String.valueOf(MyPhone.getVersionCode()))
            .set("AcftNum", Util.getAcftNum(4))
            .set("AcftTagId", Util.getAcftNum(5))
            .set("AcftName", Util.getAcftNum(6))
            .set("isFlyingPattern", String.valueOf(Props.SessionProp.pIsMultileg))
            .set("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec))
            .set("speed_thresh", String.valueOf(Math.round(SessionProp.pSpinnerMinSpeed)))
            .set("isdebug", String.valueOf(SessionProp.pIsDebug))
            .set("routeid", route.routeNumber.equals(ROUTE_NUMBER_DEFAULT)?null:route.routeNumber);
            try(
                    HttpJsonClient client = new HttpJsonClient(entityRequestNewFlight)
                    //FontLogAsync myLog = new FontLogAsync()
            ) {
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
                            isGetFlightCallSuccess = true;
                            route._legCount++;
                            set_flightNumber(response.responseNewFlightNum);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: " + e.getMessage(), 'd'));
                        //client.isFailed = true;
                        if (flightNumStatus == REMOTE_DEFAULT) if (mainactivityInstance != null) {
                            Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
                            EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                    .setEventMessageValueBool(isGetFlightCallSuccess)
                                    .setEventMessageValueString(flightNumber));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure URL method not found status code: " + statusCode, 'd'));
                        //client.isFailed = true;
                        if (flightNumStatus == REMOTE_DEFAULT) if (mainactivityInstance != null) {
                            Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
                            EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                    .setEventMessageValueBool(isGetFlightCallSuccess)
                                    .setEventMessageValueString(flightNumber));
                        }
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getNewFlightID onRetry:" + retryNo, 'd'));
                    }
                    @Override
                    public void onFinish() {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getNewFlightID onFinish", 'd'));
                    }
                  });
            }
            catch(Exception e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: ", 'e'));
            }
        }

    public void saveLocCheckSpeed(final Location location) {

        float speedCurrent = location.getSpeed();
        //new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocCheckSpeed: reported speed: " + speedCurrent, 'd'));
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = isDoubleSpeedAboveMin();
        switch (flightState) {
            case READY_TOSAVELOCATIONS:
                if (isSpeedAboveMin) set_flightState(FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN);
                break;

            case INFLIGHT_SPEEDABOVEMIN:
                if (!isElevationCheckDone) {
                    if (_flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                set_flightTimeSec();
                if (!isSpeedAboveMin) {
                    set_flightState(FLIGHT_STATE.STOPPED);
                    EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDLOW).setEventMessageValueString(flightNumber));
                }
                break;
        }
    }

    private void saveLocation(Location location, boolean iselevecheck) {
        try {
            int p = _wayPointsCount + 1;
            ContentValues values = new ContentValues();
            values.put(DBSchema.COLUMN_NAME_COL1, REQUEST_LOCATION_UPDATE); //rcode
            values.put(DBSchema.LOC_flightid, flightNumber); //flightid
            values.put(DBSchema.LOC_isTempFlight, flightNumStatus == FLIGHTNUMBER_SRC.LOCAL); //istempflightnum
            values.put(DBSchema.LOC_speedlowflag, !isSpeedAboveMin); /// speed low
            //values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString(speedCurrentInt)); //speed
            values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString((int) location.getSpeed())); //speed
            values.put(DBSchema.COLUMN_NAME_COL6, Double.toString(location.getLatitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL7, Double.toString(location.getLongitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL8, Float.toString(location.getAccuracy())); //accuracy
            values.put(DBSchema.COLUMN_NAME_COL9, Math.round(location.getAltitude())); //extrainfo
            values.put(DBSchema.LOC_wpntnum, p); //wpntnum
            values.put(DBSchema.COLUMN_NAME_COL11, Integer.toString(Util.getSignalStregth())); //gsmsignal
            values.put(DBSchema.LOC_date, URLEncoder.encode(getDateTimeNow(), "UTF-8")); //date
            values.put(DBSchema.LOC_is_elevetion_check, iselevecheck);
            long r = sqlHelper.rowLocationInsert(values);
            if (r > 0) {
                dbIdList.add((int) r);
                lastAltitudeFt = (int) (Math.round(location.getAltitude() * 3.281));
                set_wayPointsCount(p);
                new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocation: dbLocationRecCountNormal: " + SessionProp.dbLocationRecCountNormal, 'd'));
            }
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "SQLite Exception Placeholder", 'e'));
        }
    }

    public void set_flightTimeSec() {
        long elapsedTime = getTimeGMT() - _flightStartTimeGMT;
        _flightTimeSec = (int) elapsedTime / 1000;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        flightTimeString = dateFormat.format(elapsedTime);
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_FLIGHTTIME_UPDATE_COMPLETED).setEventMessageValueString(flightNumber));
        Double remainderSec = (double)_flightTimeSec/60%TIME_TALK_INTERVAL_MIN*60;
        if(0<=remainderSec && remainderSec<=SvcLocationClock.get_intervalClockSecCurrent()){
            new TalkAsync().execute(new EntityFlightTimeMessage(_flightTimeSec));
        }
    }

    double get_cutoffSpeed() {
        return SessionProp.pSpinnerMinSpeed * (RouteBase.activeFlight.flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN ? 0.75 : 1.0);
    }

    void raiseEventGetFlightComleted() {
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                .setEventMessageValueBool(isGetFlightCallSuccess)
                .setEventMessageValueString(flightNumber));
    }

    public FlightOnline set_flightState(FLIGHT_STATE fs) {
        super.set_flightState(fs);
        switch (fs) {
            case GETTINGFLIGHT:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
                break;
            case READY_TOSAVELOCATIONS:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_STATECHANGEDTO_READYTOSAVE).setEventMessageValueString(flightNumber));
                break;
            case INFLIGHT_SPEEDABOVEMIN:
                _flightStartTimeGMT = getTimeGMT();
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDABOVEMIN).setEventMessageValueString(flightNumber));
                break;
            case STOPPED:
                if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
                    set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
                }
                break;
        }
        return this;
    }

    public void set_flightState(FLIGHT_STATE fs, String descr) {
        set_flightState(fs);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightState reasoning : " + fs + ' ' + descr, 'd'));
    }

    //    void set_fAction(FACTION request) {
//        new FontLogAsync().execute(new LogMessage(TAG, flightNumber + ":fACTION :" + request, 'd');
//        lastAction = request;
////        switch (fStatus) {
////            case ACTIVE:
//        switch (request) {
////            case CHANGE_IN_PENDING:
////                EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
////                        .setEventMessageValueBool(isGetFlightCallSuccess)
////                        .setEventMessageValueString(flightNumber));
////                break;
////            case TERMINATE_GETFLIGHTNUM:
////                set_flightState(FSTATE.CLOSED);
////                //EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
////                break;
////            case CHANGE_IN_FLIGHT:
////                /// reset Timer 1 to slower rate
////                _flightStartTimeGMT = getTimeGMT();
////                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SessionProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
////                //route.set_rAction(RACTION.ON_FLIGHTTIME_CHANGED);
////                break;
//            case TERMINATE_FLIGHT:
//                //TODO
//                break;
////            case CLOSE_FLIGHT_IF_ZERO_LOCATIONS:
////                if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
////                    set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
////                }
////                break;
//        }
//    }
    @Override
    public void onClock(EventMessage eventMessage) {

        new FontLogAsync().execute(new EntityLogMessage(TAG, flightNumber + "onClock", 'd'));
        if (RouteBase.activeFlight == this
                && (flightState == FLIGHT_STATE.READY_TOSAVELOCATIONS || flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN)
                && eventMessage.eventMessageValueLocation != null) {
            //                    String s = Arrays.toString(Thread.currentThread().getStackTrace());
            //                    new FontLogAsync().execute(new LogMessage(TAG, "StackTrace: "+s,'d');
            saveLocCheckSpeed(eventMessage.eventMessageValueLocation);
        }
        if (Util.isNetworkAvailable() && !isGettingFlight) {
            if (flightNumStatus == FLIGHTNUMBER_SRC.LOCAL) getNewFlightID();
        }
        if (flightState == FLIGHT_STATE.STOPPED && sqlHelper.getLocationFlightCount(flightNumber) == 0) {
            set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
        }
    }

    @Override
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, flightNumber + ":eventReceiver:" + ev, 'd'));
        super.eventReceiver(eventMessage);
        switch (ev) {
            case SESSION_ONSUCCESS_COMMAND:
                String server_command = eventMessage.eventMessageValueString;
                new FontLogAsync().execute(new EntityLogMessage(TAG, "server_command int: " + server_command, 'd'));
                switch (server_command) {
                    case COMMAND_TERMINATEFLIGHT:
                        //isJunkFlight = true;
                        Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
                        set_flightState(FLIGHT_STATE.STOPPED, "Terminate flight server command");
                        //set_fAction(FACTION.TERMINATE_FLIGHT);
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        /// this server request is disabled for now
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        isLimitReached = true;
                        //set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
                        set_flightState(FLIGHT_STATE.STOPPED);
                        break;
                }
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                if (flightState == FLIGHT_STATE.GETTINGFLIGHT) set_flightState(FLIGHT_STATE.CLOSED);
                else set_flightState(FLIGHT_STATE.STOPPED);
                //TODO remove flight points
                break;
            case SQL_LOCALFLIGHTNUM_ALLOCATED:
                flightNumber = eventMessage.eventMessageValueString;
                flightNumStatus = FLIGHTNUMBER_SRC.LOCAL;
                isGetFlightCallSuccess = true;
                route._legCount++;
                //set_fAction(FACTION.CHANGE_IN_PENDING);
                set_flightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
                break;
        }
    }
}
