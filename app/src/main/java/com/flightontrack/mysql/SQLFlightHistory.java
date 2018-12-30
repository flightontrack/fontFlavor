package com.flightontrack.mysql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityFlightHist;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.shared.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.mysql.DbTableFlightHistory.*;
import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.definitions.EventEnums.*;

//import android.content.Context;


public class SQLFlightHistory extends SQLiteOpenHelper implements EventBus {
    private static final String TAG = "SQLFlightEntity";


    public SQLiteDatabase dbw;

    //public SQLFlightEntity(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, SQLiteDatabase dbwp) {
    public SQLFlightHistory() {
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

    public int insertFlightEntityRecord(EntityFlightHist flight){
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

    public List<EntityFlightHist> getFlightHistList() {

        dbw = getReadableDatabase();
        ArrayList<EntityFlightHist> flightList = new ArrayList<>();

        try (Cursor cu = dbw.rawQuery(SQL_SELECT_FLIGHTHISTORY_RECORDSET, new String[]{})) {
            while (cu.moveToNext()) {
                EntityFlightHist f = new EntityFlightHist();
                f.dbid = cu.getPosition();
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
            new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: "+e.getMessage(), 'e'));
        }
        finally {
            dbw.close();
        }
        return flightList;
    }
    public EntityFlightHist getFlightHistEntity(String fn) {

        dbw = getReadableDatabase();
        EntityFlightHist f = new EntityFlightHist();
        String sql = SQL_SELECT_FLIGHTENTITY_ALL + "where " + FLIGHTHIST_FlightNumber+" = "+fn;
        try (Cursor cu = dbw.rawQuery(sql, new String[]{})) {
            while (cu.moveToNext()) {
                f.dbid = cu.getPosition();
                //f.dbid = cu.getInt(cu.getColumnIndexOrThrow(_ID));
                f.flightNumber = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightNumber));
                f.routeNumber = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_RouteNumber));
                f.flightDate = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightDate));
                f.flightTimeStart = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightTimeStart));
                f.flightDuration =  cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightDuration));
                f.flightAcft = cu.getString(cu.getColumnIndexOrThrow(FLIGHTHIST_FlightAcft));
            }
        }
        catch (Exception e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: "+e.getMessage() , 'e'));
        }
        finally {
            dbw.close();
        }
        return f;
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
    public void eventReceiver(EntityEventMessage entityEventMessage){
        EVENT ev = entityEventMessage.event;
        switch(ev){
        }
    }
}
