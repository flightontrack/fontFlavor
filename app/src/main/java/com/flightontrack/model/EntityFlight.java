package com.flightontrack.model;


public class EntityFlight {
    public int i;
    public String flightNumber;
    public String routeNumber;
    public String flightTimeStart;
    public String flightDuration;
    public String flightAcft;

    public EntityFlight(){
    }

    public EntityFlight(String f,String r,String t,String a){
        flightAcft      =a;
        flightNumber    =f;
        routeNumber     =r;
        flightTimeStart =t;

    }
    public EntityFlight(String f,String r,String t,String d,String a){
        flightAcft      =a;
        flightNumber    =f;
        routeNumber     =r;
        flightTimeStart =t;
        flightDuration  =d;
    }
}
