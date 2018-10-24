package com.flightontrack.communication;

import com.flightontrack.R;
import com.flightontrack.shared.Props;

import static com.flightontrack.shared.Props.ctxApp;

/**
 * Created by hotvk on 10/23/2018.
 */

public final class URLs {
    public static String getTrackingURL() {
        String[] spinnerUrls = ctxApp.getResources().getStringArray(R.array.posturl_array);
        return "http://"+spinnerUrls[Props.SessionProp.pSpinnerUrlsPos].trim();
    }
    public static String getWebserverURL() {
        String[] url = ctxApp.getResources().getStringArray(R.array.webserverurl_array);
        //String url = ctxApp.getString(R.string.app_webserver_azure);
        return "http://"+url[Props.SessionProp.pSpinnerUrlsPos].trim();
    }
}

