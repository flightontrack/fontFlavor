package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;

import http.HttpJsonClientApiMin;
import com.flightontrack.http.ResponseJsonObj;
import model.EntityRequestHealthCheck;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.shared.EventBus;

import static com.flightontrack.definitions.EventEnums.EVENT.HEALTHCHECK_ONRESTART;
import com.flightontrack.model.EntityEventMessage;
import com.flightontrack.ui.MainActivity;
import com.flightontrack.clock.SvcLocationClock;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.shared.Props.*;
import static shared.AppConfig.*;

public class ReceiverHealthCheckAlarm extends BroadcastReceiver {
    private static final String TAG = "ReceiverHealthCheckAlarm";
    public static boolean alarmDisable = false;
    public static boolean isRestart = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isRestart = false;
        if(!MainActivity.isMainActivityExist()){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!! MainActivity is killed ...... returning",'d'));
            return;
        }

        if(alarmDisable) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!! Alarm Disabled",'d'));
            return;
        }

        if(!alarmDisable && !pIsAppTypePublic) {
            healthCheckComm();

            if (!SvcLocationClock.isServiceInstanceCreated()) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "Restarting : performClick()",'d'));
                SessionProp.set_isMultileg(true);
                EventBus.distribute(new EntityEventMessage(HEALTHCHECK_ONRESTART));
                //mainactivityInstance.trackingButton.performClick();
                isRestart = true;
                healthCheckComm();
            }

            AlarmManagerCtrl.setAlarm();
        }

    }
    void healthCheckComm(){

        try (
                HttpJsonClientApiMin client = new HttpJsonClientApiMin(new EntityRequestHealthCheck())
        )
        {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm", 'd'));
            client.post(
            new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm onSuccess", 'd'));
                                ResponseJsonObj response = new ResponseJsonObj(jsonObject);

                                if (response.isException) {
                                    new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm onSuccess|Exception|" + response.responseException, 'd'));
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm onFailure", 'd'));
                            }

                            public void onFinish() {

                            }
                        }
            );
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
