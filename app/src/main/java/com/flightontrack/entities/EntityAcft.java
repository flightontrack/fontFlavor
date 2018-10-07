package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Props;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flightontrack.definitions.SHPREF.*;

public class EntityAcft {
    private static final String TAG = "EntityAcft";

    public String AcftNum;
    public String AcftName;
    public String AcftTagId;

    public EntityAcft(){
        String acftSet = Props.sharedPreferences.getString(DEFAULTACFTSET,null);
        if (null != acftSet) try {
            JSONObject jsonDefaultAcftSet = new JSONObject(acftSet);
            AcftNum=jsonDefaultAcftSet.getString(ACFTREGNUM);
            AcftName=jsonDefaultAcftSet.getString(ACFTNAME);
        }
        catch(JSONException e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "EntityAcft() ", 'e',e));
        }
    }

    public EntityAcft(String p1,String p2,String p3){
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
}
