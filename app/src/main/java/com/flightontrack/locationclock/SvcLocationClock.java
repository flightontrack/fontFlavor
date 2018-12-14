package com.flightontrack.locationclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.flightontrack.flight.RouteBase;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.objects.MyPhone;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.GetTime;
import com.flightontrack.ui.MainActivity;
import com.flightontrack.shared.Props;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.definitions.Enums.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.shared.Props.ctxApp;

public class SvcLocationClock extends Service implements EventBus, LocationListener,GetTime {
    static final String TAG = "SvcLocationClock";
    //private static Context ctx;
    static LocationManager locationManager;
    public static SvcLocationClock instanceSvcLocationClock = null;
    static boolean isBound = false;
    static int tryCounter = 0;
    final int TRY_NUMBER = 3;
    static MODE _mode;
    static int _intervalClockSecCurrent = MIN_TIME_BW_GPS_UPDATES_SEC;
    public static int intervalClockSecPrev = _intervalClockSecCurrent;
    public static long  alarmNextTimeUTCmsec;
    MyPhone phStateListener;

    public SvcLocationClock() {
    }

    public static boolean isInstanceCreated() {
        return instanceSvcLocationClock != null;
    }
    public static SvcLocationClock getInstance() {
        return instanceSvcLocationClock;
    }

    public static boolean isBound() {
        return isBound;
    }

    public static void stopLocationUpdates() {
        new FontLogAsync().execute(new EntityLogMessage(TAG,"stopLocationUpdates : instanceSvcLocationClock = " + instanceSvcLocationClock, 'd'));
        try {
            locationManager.removeUpdates(instanceSvcLocationClock);
        }
        catch(SecurityException e ){
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
    }

    public void requestLocationUpdate(int timeSec, long distance) {

        new FontLogAsync().execute(new EntityLogMessage(TAG, "requestLocationUpdate: interval: " + timeSec + " dist: " + distance, 'd'));
        SvcLocationClock.stopLocationUpdates();
        set_intervalClockSecCurrent(timeSec);
        setClockNextTimeLocalMsec(0);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _intervalClockSecCurrent * 1000, distance, this);
        }
        catch(SecurityException e ){
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
    }
    public void set_mode(MODE m){
        _mode = m;
        switch (_mode){
            case CLOCK_ONLY:
                requestLocationUpdate(MIN_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                EventBus.distribute(new EventMessage(EVENT.CLOCK_MODECLOCK_ONLY));
                break;
            case CLOCK_LOCATION:
                requestLocationUpdate(MIN_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_MIN);
                break;
        }
    }
    public static void set_intervalClockSecCurrent(int timeSec){
        intervalClockSecPrev = _intervalClockSecCurrent;
        _intervalClockSecCurrent = timeSec;
        //Util.appendLog(TAG + "set_intervalClockSecCurrent: "+_intervalClockSecCurrent, 'd');
    }
    public static int get_intervalClockSecCurrent(){
        return _intervalClockSecCurrent;
    }
    @Override
    public void onLocationChanged(final Location location) {
        if(_mode==MODE.CLOCK_ONLY && RouteBase.activeFlight==null){
            tryCounter++;
            new FontLogAsync().execute(new EntityLogMessage(TAG,"TimerCounter:" + tryCounter,'d'));
            //FontLog.appendLog(TAG + "TimerCounter:" + tryCounter, 'd');
            if(tryCounter >TRY_NUMBER || dbLocationRecCountNormal <1) {
                tryCounter = 0;
                stopServiceSelf();
                return;
            }
        }
        else {
            tryCounter =0;
            long currTime = getTimeGMT();
            if (currTime + TIME_RESERVE >= alarmNextTimeUTCmsec) {
                //Util.appendLog(TAG + "isClockTimeReached: ", 'd');
                /// it is a protection
                setClockNextTimeLocalMsec(_intervalClockSecCurrent);
                EventBus.distribute(new EventMessage(EVENT.CLOCK_ONTICK).setEventMessageValueClockMode(_mode).setEventMessageValueLocation(location));
            }
        }
    }

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {}
    @Override
    public IBinder onBind(Intent intent) {
        isBound = false;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return true; // ensures onRebind is called
    }

    @Override
    public void onRebind(Intent intent) {
        isBound = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        phStateListener = new MyPhone();
        setSignalStrengthListener(true);
        _mode = MODE.CLOCK_LOCATION;
        requestLocationUpdate((int)MIN_TIME_BW_GPS_UPDATES/1000, DISTANCE_CHANGE_FOR_UPDATES_MIN);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!MainActivity.isMainActivityExist()){
            stopSelf();
            return;
        }
        //FontLog.appendLog(TAG + "onCreate",'d');
        new FontLogAsync().execute(new EntityLogMessage(TAG,"onCreate",'d'));
        instanceSvcLocationClock =this;
        _mode = MODE.CLOCK_LOCATION;
        EventBus.distribute(new EventMessage(EVENT.CLOCK_SERVICESTARTED_MODELOCATION));
        //ctx = getApplicationContext();
        alarmNextTimeUTCmsec = getTimeGMT();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //ReceiverHealthCheckAlarm.isRestart = true;

    }

    public  void setSignalStrengthListener(boolean start){
        if (Props.ctxApp==null) return;
        if (start) {
            ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).listen(phStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
            ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).listen(phStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.d(Const.GLOBALTAG,TAG+ "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(ctxApp, provider +" Disabled ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new FontLogAsync().execute(new EntityLogMessage(TAG,"onDestroy",'d'));
        //FontLog.appendLog(TAG + "onDestroy", 'd');
        setToNull();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        setSignalStrengthListener(false);
        //FontLog.appendLog(TAG + "onTaskRemoved: ",'d');
        new FontLogAsync().execute(new EntityLogMessage(TAG,"onTaskRemoved",'d'));
        stopServiceSelf();

    }
    public void stopServiceSelf() {
        //FontLog.appendLog(TAG + "stopServiceSelf",'d');
        new FontLogAsync().execute(new EntityLogMessage(TAG,"stopServiceSelf",'d'));
        setSignalStrengthListener(false);
        if(!(instanceSvcLocationClock ==null)){
            stopLocationUpdates();
            setToNull();
        }
        EventBus.distribute(new EventMessage(EVENT.CLOCK_SERVICESELFSTOPPED));
        stopSelf();
    }
    void setToNull(){
        instanceSvcLocationClock =null;
        //ctx=null;
        //phStateListener=null;
    }

    void setClockNextTimeLocalMsec(int intervalSec) {
        alarmNextTimeUTCmsec = getTimeGMT()+ intervalSec*1000;
    }

    @Override
    public void eventReceiver(EventMessage eventMessage){
        EVENT ev = eventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG,"eventReceiver:"+ev,'d'));
        switch(ev){
            //case FLIGHT_GETNEWFLIGHT_COMPLETED:
            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                //if (!SvcLocationClock.isInstanceCreated()) ctxApp.startService(new Intent(ctxApp, SvcLocationClock.class));
                ctxApp.startService(new Intent(ctxApp, SvcLocationClock.class));
                break;
            case SESSION_ONSUCCESS_EXCEPTION:
                stopServiceSelf();
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                set_mode(MODE.CLOCK_ONLY);
                stopServiceSelf();
                break;
            case SESSION_ONSUCCESS_COMMAND:
                if (eventMessage.eventMessageValueString.equals(COMMAND_TERMINATEFLIGHT)) set_mode(MODE.CLOCK_ONLY);
                break;
            case ROUTE_NOACTIVEROUTE:
                set_mode(MODE.CLOCK_ONLY);
                break;
            case ROUTE_ONRESTART:
                if (!eventMessage.eventMessageValueBool) set_mode(MODE.CLOCK_ONLY);
                break;
            case FLIGHT_ONSPEEDABOVEMIN:
                requestLocationUpdate(Props.SessionProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                break;
            case FLIGHT_ONSPEEDCHANGE:
                requestLocationUpdate(eventMessage.eventMessageValueInt, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                break;
        }
    }
}
