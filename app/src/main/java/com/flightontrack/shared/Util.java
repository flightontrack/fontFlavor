package com.flightontrack.shared;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.ui.SimpleSettingsActivity;
import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.entities.EntityProgressBarGetPsw;
import com.flightontrack.entities.EntityRequestGetPsw;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.definitions.SHPREF.*;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public class Util {
    private static final String TAG = "Util";

    public Util() {
    }

//     public static String getTrackingURL() {
//        String[] spinnerUrls = ctxApp.getResources().getStringArray(R.array.posturl_array);
//        return "http://"+spinnerUrls[SessionProp.pSpinnerUrlsPos].trim();
//    }
//    public static String getWebserverURL() {
//        String[] url = ctxApp.getResources().getStringArray(R.array.webserverurl_array);
//        //String url = ctxApp.getString(R.string.app_webserver_azure);
//        return "http://"+url[SessionProp.pSpinnerUrlsPos].trim();
//    }
    public static int getWayPointLimit() {
        return sharedPreferences.getInt("wayPointLimit", WAY_POINT_HARD_LIMIT);
    }

    public static void setWayPointLimit(int wp_limit) {
        editor.putInt("wayPointLimit", WAY_POINT_HARD_LIMIT > wp_limit ? WAY_POINT_HARD_LIMIT : wp_limit).commit();
    }
//
//    public static String getAcftNum(int a) {
//        String acft;
//        switch (a) {
//            case 1:
//                acft = sharedPreferences.getString("AcftMake", ctxApp.getString(R.string.default_acft_Make));
//                break;
//            case 2:
//                acft = sharedPreferences.getString("AcftModel", ctxApp.getString(R.string.default_acft_Model));
//                break;
//            case 3:
//                acft = sharedPreferences.getString("AcftSeries", ctxApp.getString(R.string.default_acft_Series));
//                break;
//            case 4:
//                acft = sharedPreferences.getString("AcftRegNum", ctxApp.getString(R.string.default_acft_N));
//                break;
//            case 5:
//                acft = sharedPreferences.getString("AcftTagId", "");
//                break;
//            case 6:
//                acft = sharedPreferences.getString("AcftName", "");
//                break;
//            default:
//                acft = sharedPreferences.getString("AcftMake", ctxApp.getString(R.string.default_acft_Make))
//                        + " " + sharedPreferences.getString("AcftModel", ctxApp.getString(R.string.default_acft_Model))
//                        + " " + sharedPreferences.getString("AcftSeries", ctxApp.getString(R.string.default_acft_Series))
//                        + " " + sharedPreferences.getString("AcftRegNum", ctxApp.getString(R.string.default_acft_N));
//        }
//        return acft;
//    }

//    public static String getPsw() {
//        return sharedPreferences.getString(CLOUDPSW,null);
//    }
//    public static void setPsw(String psw) {
//        editor.putString(CLOUDPSW, psw).commit();
//        SimpleSettingsActivity.txtPsw.setText(psw);
//    }



//    public static int getSignalStregth() {
//        return sharedPreferences.getInt("gsmsignalstrength", -1);
//    }
//
//    public static Boolean getIsOnBoot() {
//        return sharedPreferences.getBoolean("a_isOnBoot", false);
//    }



//    public static void getCloudPsw(View view){
//        try(
//                FontLogAsync mylog = new FontLogAsync();
//                EntityProgressBarGetPsw progressBar = new EntityProgressBarGetPsw(view.getContext());
//                HttpJsonClient client = new HttpJsonClient(new EntityRequestGetPsw())
//                )
//        {
//            progressBar.show();
//            client.post(new JsonHttpResponseHandler() {
//                @Override
//                public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
//                    //progressBar.dismiss();
//                    mylog.execute(new EntityLogMessage(TAG, "getCloudPsw OnSuccess", 'd'));
//                    ResponseJsonObj response = new ResponseJsonObj(jsonObject);
//                    if (response.responsePsw!=null) {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "ap=" + response.responsePsw, 'd'));
//                        setPsw(response.responsePsw);
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
//                    mylog.execute(new EntityLogMessage(TAG, "getCloudPsw onFailure:", 'd'));
//                    //progressBar.dismiss();
//                    Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
//                    //setPsw("FailedToGet");
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
//                    Log.i(TAG, "onFailure: " + e.getMessage());
//                    mylog.execute(new EntityLogMessage(TAG, "getCloudPsw onFailure:", 'd'));
//                    Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onFinish(){
//                    progressBar.dismiss();
//                }
//            }
//            );
//        }
//        catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }
}
