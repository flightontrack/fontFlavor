package com.flightontrack.entities;

import com.flightontrack.shared.Props;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class EntityAcft {
    public String AcftNum;
    public String AcftTagId;
    public String AcftName;

    public EntityAcft(String p1,String p2,String p3){
        AcftNum=p1;
        AcftName=p2;
        AcftTagId=p3;
    }
    public EntityAcft(String p1,String p2){
        AcftNum=p1;
        AcftName=p2;
    }
    public void save(){
        Set<String> autoCompleteAcftSet = Props.sharedPreferences.getStringSet("autoCompleteAcftSet",null);
        if (!AcftNum.isEmpty()) {
            if (null == autoCompleteAcftSet) autoCompleteAcftSet = new HashSet();
            JSONObject json = new JSONObject();
            try {
                json.put("AcftRegNum", AcftNum);
                json.put("AcftName", AcftName);
            } catch (JSONException e) {
                //Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ", e);
            }
            autoCompleteAcftSet.add(json.toString());
            Props.editor.putStringSet("autoCompleteAcftSet", autoCompleteAcftSet);
            Props.editor.putString("defaultAcftSet", json.toString());

            Props.editor.putString("AcftRegNum", AcftNum.trim());
            Props.editor.putString("AcftName", AcftName.trim());
            Props.editor.commit();
        }
    }
}
