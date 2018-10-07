package com.flightontrack.flight;

import android.util.Log;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.entities.EntityRequestCloseFlight;
import com.flightontrack.entities.EntityRequestNewFlightOffline;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Props;
import com.flightontrack.shared.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.shared.Props.*;

public class FlightOffline implements EventBus{
    static final String TAG = "FlightOffline";

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
    public FLIGHT_STATE flightState = FLIGHT_STATE.DEFAULT;
    public FLIGHTNUMBER_SRC flightNumStatus = FLIGHTNUMBER_SRC.REMOTE_DEFAULT;
    boolean isSpeedAboveMin = false;
    boolean isLimitReached  = false;
    //boolean isJunkFlight = false;

    public FlightOffline(){}

    FlightOffline(String fn) {
        flightNumber = fn;
    }

    public FlightOffline set_flightState(FLIGHT_STATE fs){
        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightState : " + fs, 'd'));
        if (flightState == fs) return this;
        flightState = fs;
        switch(fs){
            case GETTINGFLIGHT:
                //EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
                getNewFlightID();
                break;
            case STOPPED:
                break;
            case READY_TOBECLOSED:
                getCloseFlight();
                break;
            case CLOSING:
                break;
            case CLOSED:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
                break;
        }
        return this;
    }
    public void set_flightNumber(String fn){
        new FontLogAsync().execute(new EntityLogMessage(TAG, "set_flightNumber super fn " + fn, 'd'));
        replaceFlightNumber(fn);
        set_flightState(FLIGHT_STATE.STOPPED);
        set_flightNumStatus(FLIGHTNUMBER_SRC.REMOTE_DEFAULT);
    }
    public void set_flightNumStatus(FLIGHTNUMBER_SRC fns) {
        flightNumStatus=fns;
        switch (fns) {
            case REMOTE_DEFAULT:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_REMOTENUMBER_RECEIVED)
                        .setEventMessageValueObject(this)
                        .setEventMessageValueString(flightNumber)
                );
                break;
            case LOCAL:
                break;
        }
    }

    void getNewFlightID() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "FlightOffline-getNewFlightID: " +flightNumber, 'd'));
        EntityRequestNewFlightOffline entityRequestNewFlightOffline = new EntityRequestNewFlightOffline()
            .set("phonenumber", MyPhone.myPhoneId)
            .set("username", Pilot.getPilotUserName())
            .set("userid", Pilot.getUserID())
            .set("deviceid", MyPhone.myDeviceId)
            .set("aid", MyPhone.getMyAndroidID())
            .set("versioncode", String.valueOf(MyPhone.getVersionCode()))
            .set("AcftNum", Util.getAcftNum(4))
            .set("AcftTagId", Util.getAcftNum(5))
            .set("AcftName", Util.getAcftNum(6))
            .set("isFlyingPattern", String.valueOf(Props.SessionProp.pIsMultileg))
            .set("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec))
            .set("speed_thresh", String.valueOf(Math.round(SessionProp.pSpinnerMinSpeed)))
            .set("isdebug", String.valueOf(SessionProp.pIsDebug));
        try (HttpJsonClient client = new HttpJsonClient(entityRequestNewFlightOffline)) {
            client.post(new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i(TAG, "oonStart " + client.urlLink   );
            }

            @Override
            public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "FlightOffline-getNewFlightID OnSuccess", 'd'));

                ResponseJsonObj response = new ResponseJsonObj(jsonObject);
                if (response.responseException != null) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "RESPONSE_EXCEPTION: " + response.responseExceptionMsg, 'd'));
                    Toast.makeText(mainactivityInstance, R.string.cloud_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.responseNewFlightNum != null) {
                    {
                        set_flightNumber(response.responseNewFlightNum);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure; ", 'd'));
                if (mainactivityInstance != null){
                    Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                }
                EventBus.distribute(new EventMessage(EVENT.FLIGHTBASE_GETFLIGHTNUM).setEventMessageValueBool(false));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
                new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: " + e, 'd'));
            }

            @Override
            public void onRetry(int retryNo) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "getNewFlightID onRetry:" + retryNo, 'd'));
            }
            });
        }
        catch(Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: ", 'e'));
        }
    }

    void getCloseFlight() {
        String TAG = "getCloseFlightNew";
        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlight: " + flightNumber, 'd'));
        set_flightState(FLIGHT_STATE.CLOSING);
        EntityRequestCloseFlight entityRequestCloseFlight = new EntityRequestCloseFlight()
                .set("flightid", flightNumber)
                .set("isdebug", SessionProp.pIsDebug)
                .set("speedlowflag", !isSpeedAboveMin)
                .set("isLimitReached", isLimitReached);
                //.set("isJunkFlight", isJunkFlight);
        try (
                HttpJsonClient client= new HttpJsonClient(entityRequestCloseFlight)
        )
        {
            client.post(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlight OnSuccess", 'd'));
                    ResponseJsonObj response = new ResponseJsonObj(jsonObject);

                    if (response.responseAckn != null) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess|Flight closed: " + flightNumber, 'd'));
                    }
                    if (response.responseException != null) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess|RESPONSE_TYPE_NOTIF:" + response.responseException, 'd'));
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlight onFailure: " + flightNumber, 'd'));
                }

                @Override
                public void onFinish() {
                    set_flightState(FLIGHT_STATE.CLOSED);
                }
            }
            );
        }
        catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlightNew " + e.getMessage(), 'e'));
        }
    }


    void replaceFlightNumber(String fnew) {
        if (sqlHelper.updateTempFlightNum(flightNumber, fnew) > 0) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "replaceFlightNumber: " + flightNumber+"->" +fnew, 'd'));
        }
        else new FontLogAsync().execute(new EntityLogMessage(TAG, "replaceFlightNumber: nothing to replace: " + flightNumber+"->" +fnew, 'd'));
        flightNumber = fnew;
    }

    int getLocationFlightCount() {
         return sqlHelper.getLocationFlightCount(flightNumber);
    }
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, flightNumber + ":eventReceiver:" + ev, 'd'));
        switch (ev) {
            case SQL_FLIGHTRECORDCOUNT_ZERO:
                if (flightState == FLIGHT_STATE.STOPPED) set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
                break;
        }
    }

}
