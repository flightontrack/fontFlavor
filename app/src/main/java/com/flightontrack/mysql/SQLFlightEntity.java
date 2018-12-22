package com.flightontrack.mysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityFlight;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;

import java.util.ArrayList;
import java.util.List;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.mysql.DbTableFlightHistory.*;
import static com.flightontrack.shared.Props.SessionProp.dbLocationRecCountNormal;
import static com.flightontrack.shared.Props.SessionProp.dbTempFlightRecCount;
import static com.flightontrack.shared.Props.ctxApp;

//import android.content.Context;


public class SQLFlightEntity extends SQLiteOpenHelper implements EventBus {
    private static final String TAG = "SQLFlightEntity";


    public SQLiteDatabase dbw;

    //public SQLFlightEntity(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, SQLiteDatabase dbwp) {
    public SQLFlightEntity() {
        super(ctxApp, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            dbw = getWritableDatabase();
            dbw.execSQL(SQL_CREATE_TABLE_FLIGHTENTITY_IF_NOT_EXISTS);
        }
        catch(Exception e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "EXCEPTION!!!!: "+e.toString(), 'e'));
        }
        finally {
            dbw.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "SQLHelper:onCreate", 'd'));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        onCreate(db);
    }

    public int insertFlightEntityRecord(EntityFlight flight){
        dbw = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FLIGHTHIST_FlightNumber, flight.flightNumber);
        values.put(FLIGHTHIST_RouteNumber, flight.routeNumber);
        values.put(FLIGHTHIST_FlightDate, flight.flightDate);
        values.put(FLIGHTHIST_FlightTimeStart, flight.flightTimeStart);
        values.put(FLIGHTHIST_FlightDuration, flight.flightDuration);
        values.put(FLIGHTHIST_FlightAcft, flight.flightAcft);
        values.put(FLIGHTHIST_IsJunk, flight.isJunk);

        long r = 0;
        try {
            r = dbw.insert(TABLE_FLIGHTENTITY,
                    null,
                    values);
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return (int) r;
    }

    public List<EntityFlight> getFlightHistList() {

        dbw = getReadableDatabase();
        ArrayList<EntityFlight> flightList = new ArrayList<>();

        try (Cursor cu = dbw.rawQuery(SQL_SELECT_FLIGHTENTITY  , new String[]{})) {
            while (cu.moveToNext()) {
                EntityFlight f = new EntityFlight();
                f.i = cu.getPosition();
                f.flightNumber = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightNumber));
                f.routeNumber = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_RouteNumber));
                f.flightDate = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightDate));
                f.flightTimeStart = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightTimeStart));
                f.flightDuration =  cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightDuration));
                f.flightAcft = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightAcft));
                flightList.add(f);
            }
        }
        catch (Exception e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: ", 'e'));
        }
        finally {
            dbw.close();
        }

//        flightList.add(new EntityFlight("700000","7000","12:00pm","35 min"));
//        flightList.add(new EntityFlight("700100","7000","12:00pm","11 min"));
//        flightList.add(new EntityFlight("700200","7000","12:00pm","1 h 35 min"));
        return flightList;
    }
    public int updateFlightEntityDuration(int id, String flightduration){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DbTableFlightHistory.FLIGHTHIST_FlightDuration, flightduration);
            rn = dbw.update(
                    DbTableFlightHistory.TABLE_FLIGHTENTITY,
                    values,
                    DbTableFlightHistory._ID + "=" + id,
                    null
            );
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return rn;
    }
    public int updateFlightEntityTimeStart(int id, String starttime){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DbTableFlightHistory.FLIGHTHIST_FlightTimeStart, starttime);
            values.put(DbTableFlightHistory.FLIGHTHIST_IsJunk, 0);
            rn = dbw.update(
                    DbTableFlightHistory.TABLE_FLIGHTENTITY,
                    values,
                    DbTableFlightHistory._ID + "=" + id,
                    null
            );
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return rn;
    }
    public int updateFlightEntityJunkFlag(int id, int isJunk){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DbTableFlightHistory.FLIGHTHIST_IsJunk, isJunk);
            rn = dbw.update(
                    DbTableFlightHistory.TABLE_FLIGHTENTITY,
                    values,
                    DbTableFlightHistory._ID + "=" + id,
                    null
            );
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return rn;
    }
    public int updateFlightEntityFlightNum(int id, String flightnum){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DbTableFlightHistory.FLIGHTHIST_FlightNumber, flightnum);
            rn = dbw.update(
                    DbTableFlightHistory.TABLE_FLIGHTENTITY,
                    values,
                    DbTableFlightHistory._ID + "=" + id,
                    null
            );
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return rn;
    }
    public boolean clearFlightEntityTable(){
        boolean r= true;
        dbw = getWritableDatabase();
        try {
            dbw.execSQL(SQL_DROP_TABLE_FLIGHTENTITY);
        }
        catch (Exception e){
            Toast.makeText(ctxApp, "Failed to clear history", Toast.LENGTH_LONG).show();
            r= false;
        }
        finally {
            dbw.close();
        }
        return r;
    }
    @Override
    public void eventReceiver(EventMessage eventMessage){
        EVENT ev = eventMessage.event;
        switch(ev){
        }
    }
}
