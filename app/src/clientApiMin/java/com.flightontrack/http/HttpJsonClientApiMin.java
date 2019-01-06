package http;

import com.flightontrack.R;
import com.flightontrack.model.EntityLogMessage;
import model.EntityRequestHealthCheck;
import com.flightontrack.log.FontLogAsync;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.flightontrack.http.URLs.getTrackingURL;
import static com.flightontrack.shared.Props.ctxApp;


public class HttpJsonClientApiMin extends AsyncHttpClient implements AutoCloseable{
    static final String TAG = "HttpJsonClientApiMin";
    String url = getTrackingURL() + ctxApp.getString(R.string.webapi_controller);

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
    public void close() {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
