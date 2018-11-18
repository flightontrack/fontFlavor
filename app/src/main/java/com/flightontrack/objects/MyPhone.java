package com.flightontrack.objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.shared.Props;

import static com.flightontrack.definitions.SHPREF.CDMASIGNALSTRENGTH;
import static com.flightontrack.definitions.SHPREF.GSMSIGNALSTRENGTH;
import static com.flightontrack.shared.Props.editor;
import static com.flightontrack.shared.Props.sharedPreferences;

public class MyPhone extends PhoneStateListener {

    private static final String TAG = " MyPhone";

    public static int       versionCode;
    static String           deviceMmnufacturer = "unknown";
    static String           deviceBrand         = "unknown";
    public static String    deviceProduct       = "unknown";
    public static String    deviceModel         = "unknown";
    static String           codeName            = "unknown";
    static String           codeRelease         = "unknown";
    static int              codeSDK;

    public static String myDeviceId = null;
    public static String myPhoneId = getMyPhoneID();
    public static String phoneNumber = null;

    public MyPhone() {
        getBuildProp();
        getMyPhoneID();
    }

    static void getBuildProp(){
        deviceMmnufacturer = Build.MANUFACTURER;
        deviceBrand        = Build.BRAND;
        deviceProduct      = Build.PRODUCT;
        deviceModel        = Build.MODEL;

        codeName            = Build.VERSION.CODENAME;
        codeRelease         = Build.VERSION.RELEASE;
        codeSDK             = Build.VERSION.SDK_INT;
    }

    public static int getVersionCode() {
        try {
            versionCode = Props.ctxApp.getPackageManager().getPackageInfo(Props.ctxApp.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = -1;
        }
        return versionCode;
    }

    @SuppressLint("MissingPermission")
    static String getMyPhoneID() {
        phoneNumber = ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        myDeviceId = ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        String strId = (phoneNumber == null|| phoneNumber.isEmpty()) ? myDeviceId : phoneNumber;
        myPhoneId = strId.substring(strId.length() - 10); /// 10 digits number
        return myPhoneId;
    }

    public static String getMyAndroidID() {
        return Settings.Secure.getString(Props.ctxApp.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String   getMyAndroidVersion() {
        return  codeName +' ' +codeRelease+' ' +codeSDK;
    }


    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);
        //Util.appendLog(TAG + " onSignalStrengthsChanged: " + signalStrength,'d');
        //enableSignalStrengthListen(false);

        if (signalStrength.isGsm()) {
            setSignalStregth(GSMSIGNALSTRENGTH, signalStrength.getGsmSignalStrength());
        } else if (signalStrength.getCdmaDbm() > 0) {
            setSignalStregth(CDMASIGNALSTRENGTH, signalStrength.getCdmaDbm());
        } else {
            setSignalStregth(CDMASIGNALSTRENGTH, signalStrength.getEvdoDbm());
        }
    }

//    public  void enableSignalStrengthListen(boolean start){
//        if (Props.ctxApp==null) return;
//        if (start) {
//            ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//        } else {
//            ((TelephonyManager) Props.ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).listen(this, PhoneStateListener.LISTEN_NONE);
//        }
//    }

    public static void setSignalStregth(String name, int value) {
        try {
            editor.putInt(name, value).commit();
        }
        catch (Exception e) {
            Log.e(TAG,"!!!!!!!!!!!!!!"+e.getMessage());}
    }

    public static int getSignalStregth() {
        return sharedPreferences.getInt(GSMSIGNALSTRENGTH, -1);
    }

}
