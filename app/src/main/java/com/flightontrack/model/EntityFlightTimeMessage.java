package com.flightontrack.model;

public class EntityFlightTimeMessage {
    public int hour;
    public int min;


    public EntityFlightTimeMessage(int timeSec){
        hour = timeSec/3600;
        min = timeSec%3600/60;
    }
}
