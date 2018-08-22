package com.flightontrack.other;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;

import com.flightontrack.entities.EntityFlightTimeMessage;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;

import static com.flightontrack.shared.Props.*;


public class TalkAsync extends AsyncTask<EntityFlightTimeMessage, Void, Void> implements AutoCloseable{
    static final String TAG = "TalkAsync";
    public static TextToSpeech TalkTime;

    @Override
    public void close() throws Exception {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!!!!!!onPreExecute", 'd'));
//        TalkTime = new TextToSpeech(ctxApp, new TextToSpeech.OnInitListener(){
//            @Override
//            public void onInit(int status) {
//                new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!!!!!!onInit Status "+status, 'd'));
//            }
//        });
    }
    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!!!!!!onPostExecute", 'd'));
        //TalkTime.shutdown();
    }
    @Override
    protected Void doInBackground(EntityFlightTimeMessage... msgobj) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!!!!!!doInBackground", 'd'));

        //try {
            String flightTime= "The flight time is  "
                    + String.valueOf(msgobj[0].hour)
                    + " hour "
                    +String.valueOf(msgobj[0].min)
                    + " minute";
            //TalkTime..speak(flightTime, TextToSpeech.QUEUE_FLUSH, null,null);
            TalkTime.speak(flightTime, TextToSpeech.QUEUE_FLUSH, null);
            //TalkTime.shutdown();
//        }
//        catch(Exception e){
//
//        }
        return null;
    }
}
