package com.flightontrack.mysql;

import android.provider.BaseColumns;
import static com.flightontrack.mysql.DBSchema.*;

abstract class DbTableFlightHistory implements BaseColumns {

static final String TABLE_FLIGHTENTITY = "FlightHistory";

     static final String FLIGHTHIST_FlightNumber = "FlightNumber";
     static final String FLIGHTHIST_RouteNumber = "RouteNumber";
     static final String FLIGHTHIST_FlightTimeStart = "FlightTimeStart";
     static final String FLIGHTHIST_FlightDate = "FlightDate";
     static final String FLIGHTHIST_FlightDuration = "FlightDuration";
     static final String FLIGHTHIST_FlightAcft = "FlightAcft";
     static final String FLIGHTHIST_IsJunk = "IsJunk";

static final String SQL_CREATE_TABLE_FLIGHTENTITY_IF_NOT_EXISTS =
    "CREATE TABLE IF NOT EXISTS " + TABLE_FLIGHTENTITY + " (" +
          _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          FLIGHTHIST_FlightNumber + INT_TYPE +COMMA_SEP +
          FLIGHTHIST_RouteNumber + TEXT_TYPE +COMMA_SEP +
          FLIGHTHIST_FlightDate + TEXT_TYPE +COMMA_SEP +
          FLIGHTHIST_FlightTimeStart + TEXT_TYPE +COMMA_SEP +
          FLIGHTHIST_FlightAcft + TEXT_TYPE +COMMA_SEP +
          FLIGHTHIST_FlightDuration + TEXT_TYPE +COMMA_SEP +
          FLIGHTHIST_IsJunk + INT_TYPE +
     " )";

static final String SQL_DROP_TABLE_FLIGHTENTITY =    "DROP TABLE IF EXISTS " + TABLE_FLIGHTENTITY;

static final String SQL_SELECT_FLIGHTHISTORY_RECORDSET =
     "select " +
        _ID  + COMMA_SEP +
        FLIGHTHIST_FlightNumber  + COMMA_SEP +
        FLIGHTHIST_RouteNumber + COMMA_SEP +
        FLIGHTHIST_FlightDate + COMMA_SEP +
        FLIGHTHIST_FlightTimeStart + COMMA_SEP +
        FLIGHTHIST_FlightAcft + COMMA_SEP +
        FLIGHTHIST_FlightDuration + SPACE +
     "from" + SPACE + TABLE_FLIGHTENTITY + SPACE +
     "where" + SPACE+ FLIGHTHIST_IsJunk+" = 0" + SPACE +
     "order by" + SPACE + _ID +  SPACE +
     "desc"
             //+ SPACE+
             //"limit 5";
             ;

     static final String SQL_SELECT_FLIGHTENTITY_ALL =
             "select " +
                     _ID  + COMMA_SEP +
                     FLIGHTHIST_FlightNumber  + COMMA_SEP +
                     FLIGHTHIST_RouteNumber + COMMA_SEP +
                     FLIGHTHIST_FlightDate + COMMA_SEP +
                     FLIGHTHIST_FlightTimeStart + COMMA_SEP +
                     FLIGHTHIST_FlightAcft + COMMA_SEP +
                     FLIGHTHIST_FlightDuration + SPACE +
                     "from" + SPACE + TABLE_FLIGHTENTITY + SPACE
             //+ SPACE+
             //"limit 5";
             ;
}
