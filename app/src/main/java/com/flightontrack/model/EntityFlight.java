package com.flightontrack.model;


public class EntityFlight {
    public int i;
    public String flightNumber;
    public String routeNumber;
    public String flightTimeStart;
    public String flightDuration;

    public EntityFlight(){
    }

    public EntityFlight(String f,String r,String t){
        flightNumber    =f;
        routeNumber     =r;
        flightTimeStart =t;
    }
}
