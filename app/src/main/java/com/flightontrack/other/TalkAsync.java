package com.flightontrack.other;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;

import com.flightontrack.entities.EntityFlightTimeMessage;
import static com.flightontrack.shared.Props.*;


public class TalkAsync extends AsyncTask<EntityFlightTimeMessage, Void, Boolean> implements AutoCloseable, TextToSpeech.OnInitListener {

    TextToSpeech TalkTime;
    String ReadText;
    private int mHour, mMinute;

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub

    }
    @Override
    public void close() throws Exception {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }

    @Override
    protected Boolean doInBackground(EntityFlightTimeMessage... msgobj) {
        TalkTime = new TextToSpeech(ctxApp, this);
        //try {
            String flightTime= "The flight time is  "
                    + String.valueOf(msgobj[0].hour)
                    + " hour "
                    +String.valueOf(msgobj[0].min)
                    + " minute";
            TalkTime.speak(flightTime, TextToSpeech.QUEUE_FLUSH, null,null);
            TalkTime.shutdown();
//        }
//        catch(Exception e){
//
//        }
        return true;
    }
}
