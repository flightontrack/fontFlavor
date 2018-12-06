package com.flightontrack.model;


public class EntityFlight {
    int i;
    String flightNumber;
    String routeNumber;
    String flightTimeStart;
    String flightDuration;

    public EntityFlight(String f,String r,String t){
        flightNumber    =f;
        routeNumber     =r;
        flightTimeStart =t;
    }
}
