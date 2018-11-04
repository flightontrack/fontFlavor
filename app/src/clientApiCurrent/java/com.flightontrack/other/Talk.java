package other;

import java.util.HashMap;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.flightontrack.entities.EntityFlightTimeMessage;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;
import static com.flightontrack.shared.Props.ctxApp;

public class Talk implements TextToSpeech.OnInitListener,AutoCloseable{
    static final String TAG = "Talk";
    public static TextToSpeech tts;
    String h;
    String m;

    public Talk (EntityFlightTimeMessage msgobj) {
        h = msgobj.hour== 0? "": (String.valueOf(msgobj.hour) + " hour ");
        m = msgobj.min==1?"minute":"minutes";
        m = String.valueOf(msgobj.min) + m;
        tts = new TextToSpeech(ctxApp, this);
    }

    @Override
    public void close() {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
    @Override
    public void onInit(int status) {
        //new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!!!!!!onInit Status "+status, 'd'));
        if (status == TextToSpeech.SUCCESS){
            String flightTime= "The flight time is  "
                    + h
                    + m;
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {

                }

                @Override
                public void onDone(String s) {
                    tts.shutdown();
                }

                @Override
                public void onError(String s) {

                }
            });
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0");
            tts.speak(flightTime, TextToSpeech.QUEUE_FLUSH, null,null);
        }
    }

}
