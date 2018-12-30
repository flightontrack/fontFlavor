package com.flightontrack.mysql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.flightontrack.control.EntityFlightControl;
import com.flightontrack.control.FlightControl;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;

import java.util.ArrayList;
import java.util.List;

import static com.flightontrack.definitions.Finals.DATABASE_NAME;
import static com.flightontrack.definitions.Finals.DATABASE_VERSION;

import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.mysql.DBTableFlightContol.*;

//import android.content.Context;


public class SQLFlightControllerEntity extends SQLiteOpenHelper{
    private static final String TAG = "SQLFlightControllerEntity";


    public SQLiteDatabase dbw;

    //public SQLFlightEntity(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, SQLiteDatabase dbwp) {
    public SQLFlightControllerEntity() {
        super(ctxApp, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            dbw = getWritableDatabase();
            dbw.execSQL(SQL_CREATE_TTABLE_FLIGHTCONTROLLER_IF_NOT_EXISTS);
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

    public int insertFlightControllerEntityRecord(EntityFlightControl fc){
        dbw = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FLIGHTNUMBER, fc.flightNumber);
        values.put(ROUTENUMBER, fc.routeNumber);
        values.put(FLIGHTSTATE, fc.flightState.name());
        values.put(FLIGHTNUMBERSTATUS, fc.flightNumStatus.name());
        values.put(LEGNUMBER, fc.legNumber);
        values.put(ISJUNK, fc.isJunk);

        long r = 0;
        try {
            r = dbw.insert(TABLE_FLIGHTCONTROLLER,
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

    public int updateFlightState(int id,String fs){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBTableFlightContol.FLIGHTSTATE, fs);
            rn = dbw.update(
                    DBTableFlightContol.TABLE_FLIGHTCONTROLLER,
                    values,
                    DBTableFlightContol._ID + "=" + id,
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
    public int updateFlightNumStatus(int id,String ns){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBTableFlightContol.FLIGHTNUMBERSTATUS, ns);
            rn = dbw.update(
                    DBTableFlightContol.TABLE_FLIGHTCONTROLLER,
                    values,
                    DBTableFlightContol._ID + "=" + id,
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
    public int updateIsJunkFlag(int id, int isJunk){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBTableFlightContol.ISJUNK, isJunk);
            rn = dbw.update(
                    DBTableFlightContol.TABLE_FLIGHTCONTROLLER,
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
    public int updateFlightNum(int id, String flightnum,String route){
        int rn=0;
        try {
            dbw = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBTableFlightContol.FLIGHTNUMBER, flightnum);
            values.put(DBTableFlightContol.ROUTENUMBER, route);
            rn = dbw.update(
                    DBTableFlightContol.TABLE_FLIGHTCONTROLLER,
                    values,
                    DBTableFlightContol._ID + "=" + id,
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
    public void deleteFlightEntity(int id) {
        //String selection = DBTableFlightContol._ID +"=" + id;
        //int[] selectionArgs = {id};

        try{
            dbw = getWritableDatabase();
            dbw.delete(
                    DBTableFlightContol.TABLE_FLIGHTCONTROLLER,
                    DBTableFlightContol._ID +"=" + id,
                    null
                    //selectionArgs
            );
            dbw.close();
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.getMessage(), 'e'));
        }
    }

    public List<FlightControl> getDBFlightList() {

        dbw = getReadableDatabase();
        List<FlightControl> flightList = new ArrayList<>();

        try (Cursor cu = dbw.rawQuery(SQL_SELECT_FLIGHTCONTROLLER_RECORDSET, new String[]{})) {
            while (cu.moveToNext()) {
                FlightControl f = new FlightControl();
                f.dbid = cu.getPosition();
                f.flightNumber = cu.getString(cu.getColumnIndexOrThrow(FLIGHTNUMBER));
                f.routeNumber = cu.getString(cu.getColumnIndexOrThrow(ROUTENUMBER));
                f.flightState = EntityFlightControl.FLIGHT_STATE.valueOf(cu.getString(cu.getColumnIndexOrThrow(FLIGHTSTATE)));
                f.flightNumStatus = EntityFlightControl.FLIGHTNUMBER_SRC.valueOf(cu.getString(cu.getColumnIndexOrThrow(FLIGHTNUMBERSTATUS)));
                f.legNumber =  cu.getInt(cu.getColumnIndexOrThrow(LEGNUMBER));
                f.isJunk = cu.getInt(cu.getColumnIndexOrThrow(ISJUNK));
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
}
