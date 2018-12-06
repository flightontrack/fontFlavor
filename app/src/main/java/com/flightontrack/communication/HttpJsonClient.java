package com.flightontrack.communication;

import com.flightontrack.R;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityRequestCloseFlight;
import com.flightontrack.model.EntityRequestGetPsw;
import com.flightontrack.model.EntityRequestNewFlight;
import com.flightontrack.model.EntityRequestNewFlightOffline;
import com.flightontrack.model.EntityRequestPostLocation;
import com.flightontrack.log.FontLogAsync;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.flightontrack.shared.Props.*;
import static com.flightontrack.communication.URLs.*;

public class HttpJsonClient  extends AsyncHttpClient implements AutoCloseable{
    static final String TAG = "HttpJsonClient";
    String url = getTrackingURL() + ctxApp.getString(R.string.webapi_controller);
    //final String url = "http://10.0.2.2/PostngetWebApi/api/router/";
    //final String url = "http://192.168.1.2/PostngetWebApi/api/router/";
    String controllerMethod;
    public String urlLink;
    RequestParams requestParams;
    public boolean isFailed = false;

    public HttpJsonClient(Object entity){
        if (entity instanceof EntityRequestNewFlight) {
            controllerMethod = "PostFlightRequest";
            setMaxRetriesAndTimeout(2, 5000);
        }
        if (entity instanceof EntityRequestNewFlightOffline) {
            controllerMethod = "PostFlightRequest";
            setMaxRetriesAndTimeout(1, 1000);
        }
        if (entity instanceof EntityRequestCloseFlight) {
            controllerMethod = "PostFlightClose";
            setMaxRetriesAndTimeout(2,2000);
        }
        if (entity instanceof EntityRequestPostLocation) {
            controllerMethod = "PostLocation";
            setMaxRetriesAndTimeout(2,2000);
        }
        if (entity instanceof EntityRequestGetPsw) {
            controllerMethod = "PostGetPP";
            setMaxRetriesAndTimeout(2,1000);
        }
        requestParams = (RequestParams)entity;
        urlLink= url+controllerMethod;
    }

//    public HttpJsonClient(EntityRequestNewFlight entity){
//        controllerMethod = "PostFlightRequest";
//        setMaxRetriesAndTimeout(2,5000);
//        requestParams = entity;
//        urlLink= url+controllerMethod;
//    }
//    public HttpJsonClient(EntityRequestNewFlightOffline entity){
//        controllerMethod = "PostFlightRequest";
//        setMaxRetriesAndTimeout(1,1000);
//        requestParams = entity;
//        urlLink= url+controllerMethod;
//    }

//    public HttpJsonClient(EntityRequestCloseFlight entity){
//        controllerMethod = "PostFlightClose";
//        setMaxRetriesAndTimeout(2,2000);
//        requestParams = entity;
//        urlLink= url+controllerMethod;
//    }

//    public HttpJsonClient(EntityRequestPostLocation entity){
//        controllerMethod = "PostLocation";
//        setMaxRetriesAndTimeout(2,2000);
//        requestParams = entity;
//        urlLink= url+controllerMethod;
//    }

//    public HttpJsonClient(EntityRequestGetPsw entity){
//        controllerMethod = "PostGetPP";
//        setMaxRetriesAndTimeout(2,1000);
//        requestParams = entity;
//        urlLink= url+controllerMethod;
//    }

    public void post(AsyncHttpResponseHandler h) {
        new FontLogAsync().execute(new EntityLogMessage(TAG,"URL:  "+urlLink, 'd'));
        post(urlLink,requestParams,h);
    }

    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG,"From close -  AutoCloseable ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
