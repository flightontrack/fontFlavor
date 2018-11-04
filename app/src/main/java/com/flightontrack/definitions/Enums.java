package com.flightontrack.definitions;

public final class Enums {

    public enum ALERT_RESPONSE {
        POS,
        NEG,
        CANCEL}

    public enum MODE {
        CLOCK_LOCATION,
        CLOCK_ONLY}


    public enum BUTTONREQUEST{
        BUTTON_STATE_RED,
        BUTTON_STATE_GETFLIGHTID,
        BUTTON_STATE_YELLOW,
        BUTTON_STATE_GREEN,
        BUTTON_STATE_STOPPING;

        public static BUTTONREQUEST toMyEnum (String myEnumString) {
                return valueOf(myEnumString);
        }
    }

    public enum APPTYPE {
        PUBLIC,
        PRIVATE}


}
