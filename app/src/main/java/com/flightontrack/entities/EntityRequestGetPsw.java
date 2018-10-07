package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.loopj.android.http.RequestParams;
import static com.flightontrack.shared.Const.REQUEST_PSW;


public class EntityRequestGetPsw extends RequestParams implements AutoCloseable{

    final String TAG = "EntityRequestGetPsw";

    public EntityRequestGetPsw() {
        put("rcode", REQUEST_PSW);
        put("userid", Pilot.getUserID());
        put("phonenumber", MyPhone.myPhoneId);
        put("deviceid", MyPhone.myDeviceId);
    }

    @Override
    public void close() throws Exception {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}


