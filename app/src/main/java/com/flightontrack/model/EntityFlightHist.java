package com.flightontrack.model;


import static com.flightontrack.definitions.Finals.FLIGHT_NUMBER_DEFAULT;
import static com.flightontrack.definitions.Finals.FLIGHT_TIME_ZERO;
import static com.flightontrack.definitions.Finals.ROUTE_NUMBER_DEFAULT;
import static com.flightontrack.definitions.Finals.TIME_TALK_INTERVAL_MIN;

import com.flightontrack.mysql.SQLFlightHistory;

public class EntityFlightHist {
    //public int i;
    private SQLFlightHistory sqlFlightHistory;
    public String flightNumber  = FLIGHT_NUMBER_DEFAULT;
    public String routeNumber = ROUTE_NUMBER_DEFAULT;
    public String flightDate;
    public String flightTimeStart;
    public long   flightTimeStartGMT;
    public int    dbid;
    public int    wayPointsCount;
    public long   flightTime;
    public int    flightTimeSec;
    public int    talkTime;
    public String flightAcft;
    public String flightDuration = FLIGHT_TIME_ZERO;
    public int    isJunk = 1;

    public void setFlightTime(long ft) {
        flightTime = ft;
        flightTimeSec = (int) flightTime / 1000;
        talkTime = flightTimeSec/60/TIME_TALK_INTERVAL_MIN;
    }

    public void setIsJunk(int isJunk) {
        this.isJunk = isJunk;
        sqlFlightHistory.updateFlightEntityJunkFlag(dbid,isJunk);
    }

    public void setFlightTimeStart(String flightTimeStart) {
        this.flightTimeStart = flightTimeStart;
        sqlFlightHistory.updateFlightEntityTimeStart(dbid,flightTimeStart);
    }
    public void setFlightTimeStartGMT(long flightTimeStart) {
        this.flightTimeStartGMT = flightTimeStart;
        setIsJunk(0);
    }
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
        sqlFlightHistory.updateFlightEntityFlightNum(dbid,flightNumber);
    }
    public void setFlightDuration(String flightDuration) {
        this.flightDuration = flightDuration;
        sqlFlightHistory.updateFlightEntityDuration(dbid,flightDuration);
    }

    public EntityFlightHist(){
    }

    public EntityFlightHist(String fn){
        EntityFlightCopy(sqlFlightHistory.getFlightHistEntity(fn));
    }

    public EntityFlightHist(String f, String r, String d, String t, String a){
        flightAcft      =a;
        flightNumber    =f;
        routeNumber     =r;
        flightTimeStart =t;
        flightDate      =d;
        sqlFlightHistory = new SQLFlightHistory();
        dbid= sqlFlightHistory.insertFlightEntityRecord(this);

    }
    public EntityFlightHist(String r, String d, String a){
        flightAcft      =a;
        routeNumber     =r;
        flightDate      =d;
        sqlFlightHistory = new SQLFlightHistory();
        dbid= sqlFlightHistory.insertFlightEntityRecord(this);

    }
    private void EntityFlightCopy(EntityFlightHist e){
        this.flightNumber    =e.flightNumber;
        this.flightAcft      =e.flightAcft;
        this.routeNumber     =e.routeNumber;
        this.flightTimeStart =e.flightTimeStart;
        this.flightDuration  =e.flightDuration;
        this.dbid              =e.dbid;
    }
}
