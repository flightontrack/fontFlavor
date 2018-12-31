package com.flightontrack.objects;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.http.HttpJsonClient;
import com.flightontrack.http.ResponseJsonObj;
import com.flightontrack.model.EntityLogMessage;
import com.flightontrack.model.EntityProgressBarGetPsw;
import com.flightontrack.model.EntityRequestGetPsw;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Props;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.definitions.SHPREF.*;
import static com.flightontrack.shared.Props.*;
/**
 * Created by hotvk on 5/15/2017.
 */

public class Pilot extends MyPhone {
    private static final String TAG = "Pilot";
    static String userId = null;
    static String userName = null;

    Pilot(){
    }

    public static String getUserID() {
        getMyPhoneID();
        userId = myPhoneId + "." + myDeviceId.substring(myDeviceId.length() - 4); //combination of phone num. 4 digits of deviceid
        return userId;
    }

    public static String getUserName() {
        userName = myPhoneId.substring(0,3)+deviceBrand.substring(0,4)+ myPhoneId.substring(8);
        return userName;
    }

    public static void setPilotUserName(String un) {
        Props.editor.putString(PILOTUSERNAME, un.trim().replace(" ","")).commit();
        //editor.putString("userName", un.trim()).commit();
        //MainActivity.txtUserName.setText(un);
        //AircraftActivity.txtUserName.setText(un);
    }

    public static String getPilotUserName() {
        getBuildProp();
        getMyPhoneID();
        int deviceBrandLength = deviceBrand.length()>3?3:deviceBrand.length();
        userName = myPhoneId.substring(0,3)+deviceBrand.substring(0,deviceBrandLength).toUpperCase()+ myPhoneId.substring(3+deviceBrandLength);
        //String r = sharedPreferences.getString(PILOTUSERNAME, userName);
        return Props.sharedPreferences.getString(PILOTUSERNAME, userName);
    }

    public static void setCloudPsw(String psw) {
        editor.putString(CLOUDPSW, psw).commit();
        //SimpleSettingsActivity.txtPsw.setText(psw);
    }

    public static void getCloudPsw(View view){
        try(
                FontLogAsync mylog = new FontLogAsync();
                EntityProgressBarGetPsw progressBar = new EntityProgressBarGetPsw(view.getContext());
                HttpJsonClient client = new HttpJsonClient(new EntityRequestGetPsw())
        )
        {
            progressBar.show();
            client.post(new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                                //progressBar.dismiss();
                                mylog.execute(new EntityLogMessage(TAG, "getCloudPsw OnSuccess", 'd'));
                                ResponseJsonObj response = new ResponseJsonObj(jsonObject);
                                if (response.responsePsw!=null) {
                                    new FontLogAsync().execute(new EntityLogMessage(TAG, "ap=" + response.responsePsw, 'd'));
                                    setCloudPsw(response.responsePsw);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                mylog.execute(new EntityLogMessage(TAG, "getCloudPsw onFailure:", 'd'));
                                //progressBar.dismiss();
                                Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                                //setPsw("FailedToGet");
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
                                Log.i(TAG, "onFailure: " + e.getMessage());
                                mylog.execute(new EntityLogMessage(TAG, "getCloudPsw onFailure:", 'd'));
                                Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFinish(){
                                progressBar.dismiss();
                            }
                        }
            );
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static String getPsw() {
        return sharedPreferences.getString(CLOUDPSW,null);
    }
}
