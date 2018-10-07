package com.flightontrack.pilot;

import com.flightontrack.shared.Props;

import static com.flightontrack.definitions.SHPREF.PILOTUSERNAME;

/**
 * Created by hotvk on 5/15/2017.
 */

public class Pilot extends MyPhone {

    static String userId = null;
    static String userName = null;

    Pilot(){
    }

    public static String getUserID() {
        getMyPhoneID();
        userId = myPhoneId + "." + myDeviceId.substring(myDeviceId.length() - 4); //combination of phone num. 4 digits of deviceid
        return userId;
    }

    public static String getUserName() {
        userName = myPhoneId.substring(0,3)+deviceBrand.substring(0,4)+ myPhoneId.substring(8);
        return userName;
    }

    public static void setPilotUserName(String un) {
        Props.editor.putString(PILOTUSERNAME, un.trim().replace(" ","")).commit();
        //editor.putString("userName", un.trim()).commit();
        //MainActivity.txtUserName.setText(un);
        //AircraftActivity.txtUserName.setText(un);
    }

    public static String getPilotUserName() {
        getBuldProp();
        getMyPhoneID();
        int deviceBrandLength = deviceBrand.length()>3?3:deviceBrand.length();
        userName = myPhoneId.substring(0,3)+deviceBrand.substring(0,deviceBrandLength).toUpperCase()+ myPhoneId.substring(3+deviceBrandLength);
        //String r = sharedPreferences.getString(PILOTUSERNAME, userName);
        return Props.sharedPreferences.getString(PILOTUSERNAME, userName);
    }
}
