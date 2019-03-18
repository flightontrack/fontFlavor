package com.flightontrack.mysql;

import android.content.ContentValues;
//import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.flightontrack.model.EntityLocation;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityEventMessage;

import static com.flightontrack.definitions.Finals.DATABASE_NAME;
import static com.flightontrack.definitions.Finals.DATABASE_VERSION;
import static com.flightontrack.definitions.Finals.COMMAND_TERMINATEFLIGHT_ON_ALTITUDE;
//import static com.flightontrack.flight.RouteBase.get_FlightInstanceByNumber;
import static com.flightontrack.control.RouteControl.get_FlightInstanceByNumber;
import static com.flightontrack.mysql.DBSchema.*;
import static com.flightontrack.mysql.DBTableFlightContol.*;
import static com.flightontrack.mysql.DbTableFlightHistory.*;
//import static com.flightontrack.mysql.DBSchema.TABLE_FLIGHTNUMBER_ALLOCATION;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.definitions.EventEnums.*;
import com.flightontrack.shared.EventBus;
import com.flightontrack.objects.MyDateTime;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class SQLLocation extends SQLiteOpenHelper implements EventBus {
    private static final String TAG = "SQLHelper";

    public static SQLLocation SQLLocationInstance;
    public static SQLLocation getInstance() {
        if(SQLLocationInstance == null) {
            SQLLocationInstance = new SQLLocation();
        }
        return SQLLocationInstance;
    }

    public SQLiteDatabase dbw;

    public SQLLocation() {
        super(ctxApp, DATABASE_NAME, null, DATABASE_VERSION);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "SQLHelper:SQLHelper", 'd'));
        try {
            dbw = getWritableDatabase();
            dbw.execSQL(SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS);
            //dbw.close();

//            dbw.execSQL(SQL_CREATE_TABLE_FLIGHTNUM_ALLOC_IF_NOT_EXISTS);
//            dbw.execSQL(SQL_CREATE_TABLE_FLIGHTHISTORY_IF_NOT_EXISTS);
//            dbw.execSQL(SQL_CREATE_TTABLE_FLIGHTCONTROLLER_IF_NOT_EXISTS);
            //if (dbLocationRecCountNormal == 0 && getLocationTableCountTemp() == 0) {
            if (DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION) == 0) {
                ///dropCreateDb();
                //dbw = getWritableDatabase();
                dbw.execSQL(SQL_DROP_TABLE_FLIGHTNUMBER_ALLOC);
                dbw.execSQL(SQL_CREATE_TABLE_FLIGHTNUM_ALLOC_IF_NOT_EXISTS);
                dbw.execSQL(SQL_DROP_TABLE_FLIGHTCONTROLLER);
                dbw.execSQL(SQL_CREATE_TTABLE_FLIGHTCONTROLLER_IF_NOT_EXISTS);
                //dbw.close();
            }
            else {
                //dbw = getReadableDatabase();
                dbTempFlightRecCount = (int) DatabaseUtils.queryNumEntries(dbw, TABLE_FLIGHTNUMBER_ALLOCATION);
                //dbw.close();
                dbLocationRecCountNormal = getLocationRecCountNormal();
            }
            new FontLogAsync().execute(new EntityLogMessage(TAG, "Unsent Locations from Previous Session :  " + dbLocationRecCountNormal, 'd'));
            new FontLogAsync().execute(new EntityLogMessage(TAG, "Temp Flights Previous Session :  " + dbTempFlightRecCount, 'd'));
            //dbw.close();
        }
        catch(Exception e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "EXCEPTION!!!!: "+e.toString(), 'e'));
        }
        finally {
            dbw.close() ;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "SQLHelper:onCreate", 'd'));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
        onCreate(db);
    }
    boolean dropCreateDb(){
        boolean r= true;
        dbw = getWritableDatabase();
        try {
            int lcount = (int) DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION);
            dbw.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
            dbw.execSQL(DBSchema.SQL_DROP_TABLE_FLIGHTNUMBER_ALLOC);
            dbw.execSQL(DBTableFlightContol.SQL_DROP_TABLE_FLIGHTCONTROLLER);
            dbw.execSQL(SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS);
            dbw.execSQL(SQL_CREATE_TABLE_FLIGHTNUM_ALLOC_IF_NOT_EXISTS);
            dbw.execSQL(DBTableFlightContol.SQL_CREATE_TTABLE_FLIGHTCONTROLLER_IF_NOT_EXISTS);
            dbLocationRecCountNormal = 0;
            dbTempFlightRecCount = 0;
            //Toast.makeText(ctxApp, "Deleted " + lcount + " location points", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(ctxApp, "Failed to clear cache", Toast.LENGTH_LONG).show();
            r= false;
        }
        finally {
            dbw.close();
        }
        return r;
    }
//    public void rowLocationDelete(int id, String flightId) {
//        String selection = DBSchema.LOC_wpntnum + "= ? AND "+DBSchema.LOC_flightid +"= ?";
//        String[] selectionArgs = {String.valueOf(id),flightId};
//
//        try{
//        dbw = getWritableDatabase();
//        dbw.delete(
//                DBSchema.TABLE_LOCATION,
//                selection,
//                selectionArgs
//        );
//        selection = DBSchema.LOC_flightid +"= ?";
//        String[] selectionArgs1 = {flightId};
//        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION,selection,selectionArgs1);
//        dbw.close();
//        if (numRows==0){
//            EventBus.distribute(new EntityEventMessage( EVENT.SQL_FLIGHTRECORDCOUNT_ZERO)
//                    .setEventMessageValueObject(get_FlightInstanceByNumber(flightId))
//                    .setEventMessageValueString(flightId)
//            );
//        }
//        } catch (Exception e) {
//            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
//        }
//            dbLocationRecCountNormal = getLocationRecCountNormal();
//    }
    public void rowLocationDeleteOnId(int dbId, String flightId) {
        String selection = DBSchema._ID + "= ?";
        String[] selectionArgs = {String.valueOf(dbId)};

        try{
        dbw = getWritableDatabase();
        dbw.delete(
                DBSchema.TABLE_LOCATION,
                selection,
                selectionArgs
        );
        selection = DBSchema.LOC_flightid +"= ?";
        String[] selectionArgs1 = {flightId};
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION,selection,selectionArgs1);
        dbw.close();
        if (numRows==0){
            EventBus.distribute(new EntityEventMessage( EVENT.SQL_FLIGHTRECORDCOUNT_ZERO)
                    .setEventMessageValueObject(get_FlightInstanceByNumber(flightId))
                    .setEventMessageValueString(flightId)
            );
        }
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
            dbLocationRecCountNormal = getLocationRecCountNormal();
    }
    public void flightLocationsDelete(String flightId) {
        String selection = DBSchema.LOC_flightid +"= ?";
        String[] selectionArgs = {flightId};

        try{
            dbw = getWritableDatabase();
            dbw.delete(
                    DBSchema.TABLE_LOCATION,
                    selection,
                    selectionArgs
            );
            dbw.close();
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        dbLocationRecCountNormal = getLocationRecCountNormal();
    }
    public int allLocationsDelete() {
        int i =0;
        try{
            dbw = getWritableDatabase();
            i = dbw.delete(
                    DBSchema.TABLE_LOCATION,"1",null
            );
            dbw.close();
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        dbLocationRecCountNormal = 0;
        dbTempFlightRecCount=0;
        return i;
    }
    public long insertRowLocation(ContentValues values) {
        long r = 0;
        try {
            dbw = getWritableDatabase();
            r = dbw.insert(DBSchema.TABLE_LOCATION,
                    null,
                    values);
            dbw.close();
            //if (r>0)Route.isDbRecord=true;
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        dbLocationRecCountNormal = getLocationRecCountNormal();
        return r;
    }
    public ArrayList<EntityLocation> getAllLocationList() {

        String[] projection = {
                DBSchema._ID,
                DBSchema.COLUMN_NAME_COL1,
                DBSchema.LOC_flightid,
                DBSchema.LOC_isTempFlight,
                DBSchema.LOC_speedlowflag,
                DBSchema.COLUMN_NAME_COL4,
                DBSchema.COLUMN_NAME_COL6,
                DBSchema.COLUMN_NAME_COL7,
                DBSchema.COLUMN_NAME_COL8,
                DBSchema.COLUMN_NAME_COL9,
                DBSchema.LOC_wpntnum,
                DBSchema.COLUMN_NAME_COL11,
                DBSchema.LOC_date,
                DBSchema.LOC_is_elevetion_check
        };
        String sortOrder = DBSchema._ID;
        String selection = DBSchema.LOC_isTempFlight + "= ?";
        String[] selectionArgs = {"0"}; // { String.valueOf(newRowId) };
        dbw = getReadableDatabase();
        Cursor cu = dbw.query(
                DBSchema.TABLE_LOCATION,  // The table to query
                projection,                               // The columns to return
                selection,                               // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        ArrayList<EntityLocation> locations = new ArrayList();
        try {
            while (cu.moveToNext()) {
                EntityLocation l = new EntityLocation();
                l.i = cu.getPosition();
                l.itemId = cu.getLong(cu.getColumnIndexOrThrow(DBSchema._ID));
                l.rc = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL1));
                l.ft = cu.getString(cu.getColumnIndexOrThrow(DBSchema.LOC_flightid));
                l.sl = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.LOC_speedlowflag));
                l.sd = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4));
                l.la = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6));
                l.lo = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7));
                l.ac = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8));
                l.al = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9));
                l.wp = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.LOC_wpntnum));
                l.sg = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11));
                l.dt = cu.getString(cu.getColumnIndexOrThrow(DBSchema.LOC_date));
                l.irch = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.LOC_is_elevetion_check));
                locations.add(l);
            }
        }
        finally {
            cu.close();
            dbw.close();
        }
        return locations;
    }

    public ArrayList<EntityLocation> getFlightLocationList(String flightNum) {

        String[] projection = {
                DBSchema._ID,
                DBSchema.COLUMN_NAME_COL1,
                DBSchema.LOC_flightid,
                DBSchema.LOC_isTempFlight,
                DBSchema.LOC_speedlowflag,
                DBSchema.COLUMN_NAME_COL4,
                DBSchema.COLUMN_NAME_COL6,
                DBSchema.COLUMN_NAME_COL7,
                DBSchema.COLUMN_NAME_COL8,
                DBSchema.COLUMN_NAME_COL9,
                DBSchema.LOC_wpntnum,
                DBSchema.COLUMN_NAME_COL11,
                DBSchema.LOC_date,
                DBSchema.LOC_is_elevetion_check
        };
        String sortOrder = DBSchema._ID;
        String selection = DBSchema.LOC_flightid + "= ?";
        String[] selectionArgs = {flightNum}; // { String.valueOf(newRowId) };
        dbw = getReadableDatabase();
        ArrayList<EntityLocation> locations = new ArrayList();
        try (Cursor cu = dbw.query(
                DBSchema.TABLE_LOCATION,  // The table to query
                projection,                               // The columns to return
                selection,                               // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        )) {
            while (cu.moveToNext()) {
                EntityLocation l = new EntityLocation();
                l.i = cu.getPosition();
                l.itemId = cu.getLong(cu.getColumnIndexOrThrow(DBSchema._ID));
                l.rc = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL1));
                l.ft = cu.getString(cu.getColumnIndexOrThrow(DBSchema.LOC_flightid));
                l.sl = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.LOC_speedlowflag));
                l.sd = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4));
                l.la = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6));
                l.lo = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7));
                l.ac = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8));
                l.al = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9));
                l.wp = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.LOC_wpntnum));
                l.sg = cu.getString(cu.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11));
                l.dt = cu.getString(cu.getColumnIndexOrThrow(DBSchema.LOC_date));
                l.irch = cu.getInt(cu.getColumnIndexOrThrow(DBSchema.LOC_is_elevetion_check));
                locations.add(l);
            }
        } finally {
            dbw.close();
        }
        return locations;
    }
    public List<String> getAllFlightList() {

        List<String> flightIdList = new ArrayList<>();
        dbw = getReadableDatabase();

        try (Cursor c = dbw.rawQuery("select distinct flightid from Location", new String[]{})) {
            while (c.moveToNext()) {
                flightIdList.add(c.getString(c.getColumnIndexOrThrow(DBSchema.LOC_flightid)));
            }
        } finally {
            dbw.close();
        }
        return flightIdList;
    }
    public List<String> getTempFlightList() {

        List<String> flightIdList = new ArrayList<>();
        dbw = getReadableDatabase();

        try (Cursor c = dbw.rawQuery("select distinct flightid from Location where istempflightnum =1", new String[]{})) {
            while (c.moveToNext()) {
                flightIdList.add(c.getString(c.getColumnIndexOrThrow(DBSchema.LOC_flightid)));
            }
        } finally {
            dbw.close();
        }
        return flightIdList;
    }

    public List<String> getReadyToSendFlightList() {
        dbw = getReadableDatabase();
        ArrayList<String> flightList = new ArrayList<>();
        try (Cursor cu = dbw.rawQuery("select distinct flightid from Location where istempflightnum =0", new String[]{})) {
            while (cu.moveToNext()) {
                flightList.add(cu.getString(cu.getColumnIndexOrThrow(DBSchema.LOC_flightid)));
            }
        } finally {
            dbw.close();
        }
        return flightList;
    }
    public int getLocationTableCountTotal() {
        dbw = getWritableDatabase();
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION);
        dbw.close();
        return (int) numRows;
    }
    int getLocationRecCountNormal(){
        dbw = getReadableDatabase();
        Cursor c = dbw.rawQuery("select _id from Location where istempflightnum =0" ,new String[]{});
        c.moveToFirst();
        int numRows = c.getCount();
        c.close();
        dbw.close();
        return numRows;
    }
    public int getLocationFlightCount(String flightId) {
        dbw = getWritableDatabase();
        String selection = DBSchema.LOC_flightid +"= ?";
        String[] selectionArgs = {flightId};
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION,selection,selectionArgs);
        dbw.close();

        return (int) numRows;
    }
    String getNewTempFlightNum(){
        dbw = getWritableDatabase();

        Cursor c = dbw.rawQuery("select max(ifnull(flightNumber,0)) from "+TABLE_FLIGHTNUMBER_ALLOCATION ,new String[]{});
        c.moveToFirst();
        int f= c.getCount()<=0?1:c.getInt(0)+1;
        ContentValues values = new ContentValues();
        values.put(DBSchema.FLIGHTNUM_FlightNumber, f); //flightid
        //values.put(DBSchema.FLIGHTNUM_RouteNumber, 0);
        //values.put(DBSchema.FLIGHTNUM_FlightTimeStart, dateTime); //date

        long r = 0;
        try {
            r = dbw.insert(TABLE_FLIGHTNUMBER_ALLOCATION,
                    null,
                    values);
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            c.close();
            dbw.close();
        }

        if (r > 0) {
            dbTempFlightRecCount = f;
            new FontLogAsync().execute(new EntityLogMessage(TAG, "getNewTempFlightNum: dbTempFlightRecCount: " + dbTempFlightRecCount, 'd'));
        }
        return (String.valueOf(f));
    }

    public int updateTempFlightNum(String temp_fn,String replace_fn){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBSchema.LOC_flightid, replace_fn);
            values.put(DBSchema.LOC_isTempFlight, 0);
            rn = dbw.update(
                    DBSchema.TABLE_LOCATION,
                    values,
                    DBSchema.LOC_flightid + "=" + temp_fn,
                    null
            );
            dbLocationRecCountNormal+=rn;
            if (rn > 0) {
//                rn=0;
//                new FontLogAsync().execute(new LogMessage(TAG, "updateTempFlightNum: dbTempFlightRecCount: " + dbTempFlightRecCount, 'd');
//                rn = dbw.delete(
//                        DBSchema.TABLE_FLIGHTNUMBER_ALLOCATION,
//                        DBSchema.FLIGHTNUM_FlightNumber+"="+temp_fn,
//                        null
//
//                );
                dbTempFlightRecCount -=1;
            }
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return rn;
    }
    @Override
    public void eventReceiver(EntityEventMessage entityEventMessage){
        EVENT ev = entityEventMessage.event;
        switch(ev){
            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:
                if(dropCreateDb()){
                    EventBus.distribute(new EntityEventMessage(EVENT.SQL_ONCLEARCACHE_COMPLETED).setEventMessageValueBool(true));
                }
                break;
            case SESSION_ONSUCCESS_COMMAND:
                if (entityEventMessage.eventMessageValueString.equals(COMMAND_TERMINATEFLIGHT_ON_ALTITUDE)) flightLocationsDelete(entityEventMessage.eventMessageValueString);
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if(!entityEventMessage.eventMessageValueBool)
                    try {
                        String dt = URLEncoder.encode(new MyDateTime().dateTimeLocalString, "UTF-8");
                        EventBus.distribute(new EntityEventMessage(EVENT.SQL_LOCALFLIGHTNUM_ALLOCATED).setEventMessageValueString(getNewTempFlightNum()));
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                break;
        }
    }
}
