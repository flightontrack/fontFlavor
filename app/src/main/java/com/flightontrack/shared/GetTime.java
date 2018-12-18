package com.flightontrack.shared;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class GetTime {
    public String timeLocal;
    public String dateLocal;
    public String dateTimeLocal;
    public String timeDiff;
    public long   initDateTimeGMT;
    public long   dateTimeGMT;

    public GetTime() {
        long currTime = new Date().getTime();
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        dateTimeLocal = dateTimeFormat.format(currTime);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getDefault());
        dateLocal = dateFormat.format(currTime);

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getDefault());
        timeLocal = timeFormat.format(currTime);

        initDateTimeGMT =new Date().getTime();
        //return dateTimeFormat.format(currTime);
    }
    public GetTime updateDateTimeLocal() {
        long currTime = new Date().getTime();
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        dateTimeLocal = dateTimeFormat.format(currTime);
        return this;
    }

    public long getTimeGMT() {
        dateTimeGMT = new Date().getTime();
        return dateTimeGMT;
    }

    public long getElapsedTime() {
        long timeDiffLong = getTimeGMT() - initDateTimeGMT;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        timeDiff = dateFormat.format(timeDiffLong);
        return timeDiffLong;
    }

//    void speakTime(){
//
//
//    }
}
