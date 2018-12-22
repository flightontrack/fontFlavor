package com.flightontrack.shared;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class GetTime {
    public String timeLocal;
    public String dateLocal;
    public String dateTimeLocalString;
    public long   dateTimeGMT;
    //public int elapsedTimeSec;

    public GetTime() {
        long currTime = new Date().getTime();
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        dateTimeLocalString = dateTimeFormat.format(currTime);

        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        dateFormat.setTimeZone(TimeZone.getDefault());
        dateLocal = dateFormat.format(currTime);

        DateFormat timeFormat = new SimpleDateFormat("H:mm a");
        timeFormat.setTimeZone(TimeZone.getDefault());
        timeLocal = timeFormat.format(currTime);

        //initDateTimeGMT =new Date().getTime();
    }
    public GetTime updateDateTime() {
        dateTimeGMT = new Date().getTime();
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        dateTimeLocalString = dateTimeFormat.format(dateTimeGMT);

        DateFormat timeFormat = new SimpleDateFormat("H:mm a");
        timeFormat.setTimeZone(TimeZone.getDefault());
        timeLocal = timeFormat.format(dateTimeGMT);

        return this;
    }

    public long getTimeGMT() {
        dateTimeGMT = new Date().getTime();
        return dateTimeGMT;
    }

    public String getElapsedTimeString(long timeDiffLong) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        //elapsedTimeSec = (int) timeDiffLong / 1000;
        return dateFormat.format(timeDiffLong);
    }

//    void speakTime(){
//
//
//    }
}
