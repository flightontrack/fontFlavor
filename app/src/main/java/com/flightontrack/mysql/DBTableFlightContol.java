package com.flightontrack.mysql;

import android.provider.BaseColumns;
import static com.flightontrack.mysql.DBSchema.*;

public abstract class DBTableFlightContol implements BaseColumns {

static final String TABLE_FLIGHTCONTROLLER= "FlightController";

static final String FLIGHTNUMBER = "FlightNumber";
static final String ROUTENUMBER = "RouteNumber";
static final String FLIGHTSTATE = "FlightState";
static final String FLIGHTNUMBERSTATUS = "FlightNumStatus";
static final String LEGNUMBER = "LegNumber";
static final String ISJUNK = "IsJunk";

public static final String SQL_CREATE_TTABLE_FLIGHTCONTROLLER_IF_NOT_EXISTS =
"CREATE TABLE IF NOT EXISTS " + TABLE_FLIGHTCONTROLLER + " (" +
     _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        FLIGHTNUMBER + TEXT_TYPE +COMMA_SEP +
        ROUTENUMBER + TEXT_TYPE +COMMA_SEP +
        FLIGHTSTATE + TEXT_TYPE +COMMA_SEP +
        FLIGHTNUMBERSTATUS + TEXT_TYPE +COMMA_SEP +
        LEGNUMBER + INT_TYPE + COMMA_SEP +
        ISJUNK + INT_TYPE +
     " )";

public static final String SQL_DROP_TABLE_FLIGHTCONTROLLER =    "DROP TABLE IF EXISTS " + TABLE_FLIGHTCONTROLLER;

    static final String SQL_SELECT_FLIGHTCONTROLLER_RECORDSET =
            "select " +
                    _ID  + COMMA_SEP +
                    FLIGHTNUMBER  + COMMA_SEP +
                    ROUTENUMBER + COMMA_SEP +
                    FLIGHTSTATE + COMMA_SEP +
                    FLIGHTNUMBERSTATUS + COMMA_SEP +
                    LEGNUMBER + COMMA_SEP +
                    "from" + SPACE + TABLE_FLIGHTCONTROLLER + SPACE +
                    "where" + SPACE+ ISJUNK+" = 0" + SPACE
            //+ SPACE+
            //"limit 5";
            ;
}
