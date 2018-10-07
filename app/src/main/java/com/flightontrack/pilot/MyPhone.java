package com.flightontrack.pilot;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.flightontrack.shared.Props;

public class MyPhone {

    //static Context  ctx;
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
        getBuldProp();
        getMyPhoneID();
    }

    static void getBuldProp(){
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
}
