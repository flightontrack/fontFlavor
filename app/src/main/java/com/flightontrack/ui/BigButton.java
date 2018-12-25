package com.flightontrack.ui;

import com.flightontrack.R;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.Props;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.definitions.Enums.*;
import static com.flightontrack.shared.Props.SessionProp.trackingButtonState;
import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Props.mainactivityInstance;
import static com.flightontrack.definitions.EventEnums.*;

public class BigButton implements EventBus {
    static final String TAG = "BigButton";
    public static BigButton bigButtonInstance = null;

    public static BigButton getInstance() {
        if(bigButtonInstance == null) {
            bigButtonInstance = new BigButton();
        }
        return bigButtonInstance;
    }

    static void setTrackingButton(BUTTONREQUEST request) {
        //int backgroundResource;
        if (request!= BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID)trackingButtonState = request;
//        else {
//            GradientDrawable myDrawable = (GradientDrawable) mainactivityInstance.trackingButton.getBackground();
//            myDrawable.setStroke(20, ctxApp.getResources().getColor(R.color.colorPrimary));
//        }
        int backgroundResource =
                request == BUTTONREQUEST.BUTTON_STATE_RED?R.drawable.bttn_status_red:
                request == BUTTONREQUEST.BUTTON_STATE_YELLOW?R.drawable.bttn_status_yellow:
                request == BUTTONREQUEST.BUTTON_STATE_GREEN?R.drawable.bttn_status_green:R.drawable.bttn_status_red;
        mainactivityInstance.trackingButton.setText(getButtonText(request));
        mainactivityInstance.trackingButton.setBackgroundResource(backgroundResource);
        //(request == BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID)?R.drawable.bttn_status_red:
//        switch (request) {
//            case BUTTON_STATE_RED:
//                backgroundResource = R.drawable.bttn_status_red;
//                break;
//            case BUTTON_STATE_YELLOW:
//                backgroundResource = R.drawable.bttn_status_yellow;
//                break;
//            case BUTTON_STATE_GREEN:
//                backgroundResource = R.drawable.bttn_status_green;
//                break;
//            case BUTTON_STATE_GETFLIGHTID:
//                backgroundResource = R.drawable.bttn_status_red;
//                break;
//            default:
//                backgroundResource = R.drawable.bttn_status_red;
//
//        }
    }

    static String setTextGreen() {
        return "Flight: " + (RouteBase.activeFlight.entityFlight.flightNumber) + '\n' +
                "Point: " + RouteBase.activeFlight.entityFlight.wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time) + SPACE + RouteBase.activeFlight.getFlightTime() + '\n'
                + "Alt: " + RouteBase.activeFlight.lastAltitudeFt + " ft";
    }

    static String setTextRedFlightStopped() {
        String fText;
        String fTime = "";
        String flightN = FLIGHT_NUMBER_DEFAULT;

        if (RouteBase.activeRoute == null) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "setTextRedFlightStopped: activeRoute == null", 'd'));
            fText = Props.SessionProp.pTrackingButtonText;
        } else {
            if (RouteBase.activeFlight != null) {
                flightN = RouteBase.activeFlight.entityFlight==null?FLIGHT_NUMBER_DEFAULT:RouteBase.activeFlight.entityFlight.flightNumber;
                fTime = ctxApp.getString(R.string.tracking_flight_time) + SPACE + RouteBase.activeFlight.getFlightTime();
            }
            fText = "Flight " + flightN + '\n' + "Stopped"; // + '\n';
        }
        return fText + fTime;
    }

    static String getButtonText(BUTTONREQUEST request) {
        switch (request) {
            case BUTTON_STATE_RED:
                Props.SessionProp.pTrackingButtonText=setTextRedFlightStopped();
                break;
            case BUTTON_STATE_YELLOW:
                Props.SessionProp.pTrackingButtonText="Flight " + (RouteBase.activeFlight.entityFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff);
                break;
            case BUTTON_STATE_GREEN:
                Props.SessionProp.pTrackingButtonText=setTextGreen();
                break;
            case BUTTON_STATE_GETFLIGHTID:
                Props.SessionProp.pTrackingButtonText=ctxApp.getString(R.string.tracking_gettingflight);
                break;
        }
        return Props.SessionProp.pTrackingButtonText;
    }

    @Override
    public void eventReceiver(EntityEventMessage entityEventMessage) {
        EVENT ev = entityEventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, "eventReceiver : " + ev, 'd'));
        //txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
        switch (ev) {
            case FLIGHT_GETNEWFLIGHT_STARTED:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                break;
//            case SESSION_ONSUCCESS_EXCEPTION:;
//                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_RED);
//                break;
            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                break;
            case CLOCK_MODECLOCK_ONLY:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_RED);
                break;
//            case CLOCK_SERVICESTARTED_MODELOCATION:
//                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_YELLOW);
//                break;
//            case CLOCK_SERVICESELFSTOPPED:
//                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_RED);
//                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_GREEN);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                /// swithch to red
                break;
            case ROUTE_NOACTIVEROUTE:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_RED);
                break;
        }
    }

}
