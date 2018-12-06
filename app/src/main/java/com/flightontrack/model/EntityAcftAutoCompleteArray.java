package com.flightontrack.model;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.flightontrack.definitions.SHPREF.*;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Props;

public class EntityAcftAutoCompleteArray {
    private static final String TAG = "EntityAcftAutoComplete";

    private String AcftNum;
    private String AcftName;

    private Set<String> autoCompleteAcftSet;
    private ArrayList autoCompleteAcftArray = new ArrayList<String>();
    public ArrayList acftNumArrayList = new ArrayList<String>();
    public ArrayList acftNameArrayList = new ArrayList<String>();
    public String[] acftNumArray = new String[100];

    public EntityAcftAutoCompleteArray(){
        autoCompleteAcftSet = Props.sharedPreferences.getStringSet(AUTOCOMPLETEACFTSET,null);
        if (null!=autoCompleteAcftSet) {
            for (String acft:autoCompleteAcftSet) {
                autoCompleteAcftArray.add(acft);
                try {
                    JSONObject json = new JSONObject(acft);
                    String acftNum = json.getString(ACFTREGNUM);
                    acftNumArrayList.add(acftNum);
                    String acftName = json.getString(ACFTNAME);
                    acftNameArrayList.add(acftName);
                }
                catch (JSONException e) {
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "EntityAcftAutoCompleteArray() " + e.getMessage(), 'e',e));
                }
            }
            acftNumArray = (String[]) acftNumArrayList.toArray(new String[0]);
        }
    }

    public EntityAcftAutoCompleteArray setAcftName(String acftName) {
        AcftName = acftName;
        return this;
    }

    public EntityAcftAutoCompleteArray setAcftNum(String acftNum) {
        AcftNum = acftNum;
        return this;
    }

    public void save(){
        if (!AcftNum.isEmpty()) {
            if (acftNumArrayList.contains(AcftNum)){
                autoCompleteAcftArray.remove(acftNumArrayList.indexOf(AcftNum));
            }
            JSONObject json = new JSONObject();
            try {
                json.put(ACFTREGNUM, AcftNum);
                json.put(ACFTNAME, AcftName);
            } catch (JSONException e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "save() ", 'e',e));
            }
            autoCompleteAcftArray.add(json.toString());
            autoCompleteAcftSet = new HashSet(autoCompleteAcftArray);
            Props.editor.putStringSet(AUTOCOMPLETEACFTSET, autoCompleteAcftSet).commit();
        }
    }
}
