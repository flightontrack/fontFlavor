package com.flightontrack.communication;

import com.flightontrack.R;
import com.flightontrack.entities.EntityLogMessage;
import entities.EntityRequestHealthCheck;
import com.flightontrack.log.FontLogAsync;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Util.getTrackingURL;

public class HttpJsonClientApiMin extends AsyncHttpClient implements AutoCloseable{
    static final String TAG = "HttpJsonClientApiMin";
    String url = getTrackingURL() + ctxApp.getString(R.string.webapi_controller);
    //final String url = "http://10.0.2.2/PostngetWebApi/api/router/";
    //final String url = "http://192.168.1.2/PostngetWebApi/api/router/";
    String controllerMethod;
    public String urlLink;
    RequestParams requestParams;
    public boolean isFailed = false;

    public HttpJsonClientApiMin(EntityRequestHealthCheck entity){
        controllerMethod = "HealthCheck";
        setMaxRetriesAndTimeout(1,1000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public void post(AsyncHttpResponseHandler h) {
        new FontLogAsync().execute(new EntityLogMessage(TAG,"URL:  "+urlLink, 'd'));
        post(urlLink,requestParams,h);
    }

    @Override
    public void close() throws Exception {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
