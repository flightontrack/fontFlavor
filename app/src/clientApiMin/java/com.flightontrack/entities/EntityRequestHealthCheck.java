package entities;

import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.objects.MyPhone;

import receiver.ReceiverBatteryLevel;
import com.flightontrack.shared.Props;
import com.loopj.android.http.RequestParams;

import static receiver.ReceiverHealthCheckAlarm.isRestart;
import static com.flightontrack.shared.Const.REQUEST_IS_CLOCK_ON;

/**
 * Created by hotvk on 4/29/2018.
 */

public class EntityRequestHealthCheck   extends RequestParams implements AutoCloseable {

    final String TAG = "EntityRequestGetPsw";

    public EntityRequestHealthCheck() {
        put("rcode", REQUEST_IS_CLOCK_ON);
        put("phonenumber", MyPhone._myPhoneId);
        put("deviceid", MyPhone._myDeviceId);
        put("isdebug", Props.SessionProp.pIsDebug);
        put("isClockOn", SvcLocationClock.isInstanceCreated());
        put("isrestart", isRestart);
        put("battery", ReceiverBatteryLevel.getBattery());

    }

    @Override
    public void close() throws Exception {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}
