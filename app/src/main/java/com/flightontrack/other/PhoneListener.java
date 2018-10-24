package com.flightontrack.other;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.shared.Props;
import com.flightontrack.shared.Util;

import static com.flightontrack.definitions.SHPREF.*;
import static com.flightontrack.shared.Props.editor;

public class PhoneListener extends PhoneStateListener
{

    public PhoneListener(){
        //PhoneListener.ctx=ctx;
    }
    //static Context ctx;
    private static final String TAG = " PhoneListener";
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);
        //Util.appendLog(TAG + " onSignalStrengthsChanged: " + signalStrength,'d');
        enableSignalStrengthListen(false);

        if (signalStrength.isGsm()) {
            setSignalStregth(GSMSIGNALSTRENGTH, signalStrength.getGsmSignalStrength());
        } else if (signalStrength.getCdmaDbm() > 0) {
            setSignalStregth(CDMASIGNALSTRENGTH, signalStrength.getCdmaDbm());
        } else {
            setSignalStregth(CDMASIGNALSTRENGTH, signalStrength.getEvdoDbm());
        }
    }

    public static void enableSignalStrengthListen(boolean start){
        if (Props.ctxApp==null) return;
        if (start) {
            ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).listen(SvcLocationClock.phStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
            ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).listen(SvcLocationClock.phStateListener, PhoneStateListener.LISTEN_NONE);
        }

    }

    public static void setSignalStregth(String name, int value) {
        try {
            editor.putInt(name, value).commit();
        }
        catch (Exception e) {
            Log.e(TAG,"!!!!!!!!!!!!!!"+e.getMessage());}
    }

}

