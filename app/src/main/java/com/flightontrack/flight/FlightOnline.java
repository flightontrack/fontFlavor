package com.flightontrack.flight;

import android.content.ContentValues;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.definitions.Limits;
import com.flightontrack.model.EntityFlight;
import com.flightontrack.objects.Aircraft;
import com.flightontrack.model.EntityFlightTimeMessage;
import com.flightontrack.model.EntityRequestNewFlight;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.objects.MyPhone;
import com.flightontrack.objects.Pilot;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.GetTime;
import com.flightontrack.shared.Props;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import other.Talk;

import static com.flightontrack.flight.FlightOffline.FLIGHTNUMBER_SRC.REMOTE_DEFAULT;
import static com.flightontrack.flight.Session.isNetworkAvailable;
import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class FlightOnline extends FlightOffline implements GetTime, EventBus {
    static final String TAG = "FlightOnline";

    public String   flightTimeString;
    public int      lastAltitudeFt;
    public int      wayPointsCount;
    private Route           route;
    private float           speedCurrent = 0;
    private float           speedPrev = 0;
    private int             flightTimeSec;
    private int             talkCount;
    private long            flightStartTimeGMT;
    private boolean         isElevationCheckDone;
    private boolean         isGettingFlight = false;
    private boolean         isGetFlightCallSuccess = false;
    private EntityFlight    entityFlight;
    private List<Integer>   dbIdList = new ArrayList<>();

    public FlightOnline(Route r) {
        route = r;
        flightTimeString = FLIGHT_TIME_ZERO;
        isElevationCheckDone = false;
        RouteBase.activeFlight = this;
        change_flightState(FLIGHT_STATE.GETTINGFLIGHT);
    }

    @Override
    public void set_flightNumber(String fn) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, " set_flightNumber " + fn + " flightNumStatus " + flightNumStatus, 'd'));
        //replace_FlightNumber(fn);
        flightNumber = fn;
        isGetFlightCallSuccess = true;
        route._legCount++;
        change_flightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
    }

    void set_wayPointsCount(int pointsCount) {
        wayPointsCount = pointsCount;
        if (pointsCount >= Limits.getWayPointLimit()) {
            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONPOINTSLIMITREACHED));
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
            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDCHANGE).setEventMessageValueInt(intervalClockSec));
            return true;
        }
        return false;
    }

    @Override
    void get_NewFlightID() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "FlightOnline-get_NewFlightID: " +flightNumber, 'd'));
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
            .set("isFlyingPattern", String.valueOf(Props.SessionProp.pIsMultileg))
            .set("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec))
            .set("speed_thresh", String.valueOf(Math.round(SessionProp.pSpinnerMinSpeed)))
            .set("isdebug", String.valueOf(SessionProp.pIsDebug))
            .set("routeid", route.routeNumber.equals(ROUTE_NUMBER_DEFAULT)?null:route.routeNumber);

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

                        if (flightNumStatus == FLIGHTNUMBER_SRC.LOCAL){
                            /// in case of request resubmitting for flight number on this flight
                            flightNumStatus = FLIGHTNUMBER_SRC.REMOTE_DEFAULT;
                            replace_FlightNumber(response.responseNewFlightNum);
                            route.routeNumber = response.responseNewFlightNum;
                        }
                        else {
                            /// normal flow
                            set_flightNumber(response.responseNewFlightNum);
                        }
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

    public void saveLocCheckSpeed(final Location location) {

        float speedCurrent = location.getSpeed();
        //new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocCheckSpeed: reported speed: " + speedCurrent, 'd'));
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = is_DoubleSpeedAboveMin();
        switch (flightState) {
            case READY_TOSAVELOCATIONS:
                if (isSpeedAboveMin) change_flightState(FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN);
                break;

            case INFLIGHT_SPEEDABOVEMIN:
                if (!isElevationCheckDone) {
                    if (flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                set_flightTimeSec();
                if (!isSpeedAboveMin) {
                    change_flightState(FLIGHT_STATE.STOPPED);
                    EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDLOW).setEventMessageValueString(flightNumber));
                }
                break;
        }
    }

    private void saveLocation(Location location, boolean iselevecheck) {
        try {
            int p = wayPointsCount + 1;
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
            values.put(DBSchema.COLUMN_NAME_COL11, Integer.toString(Pilot.getSignalStregth())); //gsmsignal
            values.put(DBSchema.LOC_date, URLEncoder.encode(getDateTimeNow(), "UTF-8")); //date
            values.put(DBSchema.LOC_is_elevetion_check, iselevecheck);
            long r = sqlHelper.insertRowLocation(values);
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
        long elapsedTime = getTimeGMT() - flightStartTimeGMT;
        flightTimeSec = (int) elapsedTime / 1000;
        new FontLogAsync().execute(new EntityLogMessage(TAG,"flightTimeSec =  "+flightTimeSec, 'd'));
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        flightTimeString = dateFormat.format(elapsedTime);
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_FLIGHTTIME_UPDATE_COMPLETED).setEventMessageValueString(flightNumber));
        int tc = flightTimeSec/60/TIME_TALK_INTERVAL_MIN;
        //new FontLogAsync().execute(new EntityLogMessage(TAG,"c =  "+c, 'd'));
        //new FontLogAsync().execute(new EntityLogMessage(TAG,"talkCount =  "+talkCount, 'd'));
        if (tc>talkCount){
            talkCount=tc;
            new Talk(new EntityFlightTimeMessage(flightTimeSec));
        }
    }

    double get_cutoffSpeed() {
        return SessionProp.pSpinnerMinSpeed * (RouteBase.activeFlight.flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN ? 0.75 : 1.0);
    }

    @Override
    public FlightOnline change_flightState(FLIGHT_STATE fs) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "change_flightState: current: " +flightState+" change to: "+ fs, 'd'));
        if (flightState == fs) return this;
        flightState = fs;
        switch (fs) {
            case GETTINGFLIGHT:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
                get_NewFlightID();
                break;
            case READY_TOSAVELOCATIONS:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_STATECHANGEDTO_READYTOSAVE).setEventMessageValueString(flightNumber));
                break;
            case INFLIGHT_SPEEDABOVEMIN:
                flightStartTimeGMT = getTimeGMT();
                entityFlight = new EntityFlight(flightNumber,route.routeNumber,getDateTimeNow());
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDABOVEMIN).setEventMessageValueString(flightNumber));
                break;
            case STOPPED:
                if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
                    super.change_flightState(FLIGHT_STATE.READY_TOBECLOSED);
                }
                break;
            case READY_TOBECLOSED:
                sqlHelper.insertFlightHistRecord(entityFlight);
                List<EntityFlight> l = sqlHelper.getFlightHistList();
                new FontLogAsync().execute(new EntityLogMessage(TAG, "history list size: "+l.size(), 'd'));
                get_CloseFlight();
                break;
            case CLOSING:
                break;
            case CLOSED:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
                break;
            default:
                super.change_flightState(fs);
                break;
        }
        return this;
    }

    public void set_flightState(FLIGHT_STATE fs, String descr) {
        change_flightState(fs);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightState reasoning : " + fs + ' ' + descr, 'd'));
    }

    @Override
    public void onClock(EventMessage eventMessage) {

        new FontLogAsync().execute(new EntityLogMessage(TAG, "onClock "+flightNumber+" afs:"+flightState+" loccount:"+sqlHelper.getLocationFlightCount(flightNumber), 'd'));
        if (RouteBase.activeFlight == this
                && (flightState == FLIGHT_STATE.READY_TOSAVELOCATIONS || flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN)
                && eventMessage.eventMessageValueLocation != null) {
            //                    String s = Arrays.toString(Thread.currentThread().getStackTrace());
            //                    new FontLogAsync().execute(new LogMessage(TAG, "StackTrace: "+s,'d');
            saveLocCheckSpeed(eventMessage.eventMessageValueLocation);
        }
        if (isNetworkAvailable() && !isGettingFlight) {
            if (flightNumStatus == FLIGHTNUMBER_SRC.LOCAL) get_NewFlightID();
        }
        if (flightState == FLIGHT_STATE.STOPPED && sqlHelper.getLocationFlightCount(flightNumber) == 0) {
            change_flightState(FLIGHT_STATE.READY_TOBECLOSED);
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
                        change_flightState(FLIGHT_STATE.STOPPED);
                        break;
                }
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                if (flightState == FLIGHT_STATE.GETTINGFLIGHT) change_flightState(FLIGHT_STATE.CLOSED);
                else change_flightState(FLIGHT_STATE.STOPPED);
                //TODO remove flight points
                break;
            case SQL_LOCALFLIGHTNUM_ALLOCATED:
                //flightNumber = eventMessage.eventMessageValueString;
                flightNumStatus = FLIGHTNUMBER_SRC.LOCAL;
                set_flightNumber(eventMessage.eventMessageValueString);
                //isGetFlightCallSuccess = true;
                //route._legCount++;
                //change_flightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                entityFlight.flightDuration = flightTimeString;
                break;
        }
    }
}
