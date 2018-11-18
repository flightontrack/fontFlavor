package com.flightontrack.objects;

import com.flightontrack.entities.EntityAcftAutoCompleteArray;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Props;

//import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.flightontrack.definitions.SHPREF.*;

public class Aircraft implements AutoCloseable{
    private static final String TAG = "Aircraft";

    public String AcftNum;
    public String AcftName;
    public String AcftTagId;

    public Aircraft(){
        String acftSet = Props.sharedPreferences.getString(DEFAULTACFTSET,null);
        if (null != acftSet) try {
            JSONObject jsonDefaultAcftSet = new JSONObject(acftSet);
            String names = jsonDefaultAcftSet.names().toString();

            AcftNum=jsonDefaultAcftSet.getString(ACFTREGNUM);
            AcftName=jsonDefaultAcftSet.getString(ACFTNAME);
            if (names.contains(ACFTTAGID)) AcftTagId=jsonDefaultAcftSet.getString(ACFTTAGID);
        }
        catch(JSONException e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "Aircraft() " +e.getMessage(), 'e',e));
        }
    }

    public Aircraft(String p1, String p2, String p3){
        AcftNum=p1.replace(" ","");
        if(null!=p2) AcftName=p2.trim();
        AcftTagId=p3;
    }

    public void save(){

        if (!AcftNum.isEmpty()) {
            new EntityAcftAutoCompleteArray()
                    .setAcftNum(AcftNum).setAcftName(AcftName)
                    .save();
            JSONObject json = new JSONObject();
            try {
                json.put(ACFTREGNUM, AcftNum);
                json.put(ACFTNAME, AcftName);
            } catch (JSONException e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "save() ", 'e',e));
            }
            Props.editor.putString(DEFAULTACFTSET, json.toString());
        }
        else {
            Props.editor.remove(DEFAULTACFTSET);
        }
        Props.editor.commit();
    }
    @Override
    public void close() {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
