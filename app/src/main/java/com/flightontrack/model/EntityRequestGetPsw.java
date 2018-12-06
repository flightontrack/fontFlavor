package com.flightontrack.model;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.objects.MyPhone;
import com.flightontrack.objects.Pilot;
import com.loopj.android.http.RequestParams;
import static com.flightontrack.definitions.Finals.REQUEST_PSW;


public class EntityRequestGetPsw extends RequestParams implements AutoCloseable{

    final String TAG = "EntityRequestGetPsw";

    public EntityRequestGetPsw() {
        put("rcode", REQUEST_PSW);
        put("userid", Pilot.getUserID());
        put("phonenumber", MyPhone.myPhoneId);
        put("deviceid", MyPhone.myDeviceId);
    }

    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}


