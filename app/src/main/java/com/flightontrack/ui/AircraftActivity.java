package com.flightontrack.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.flightontrack.R;
import com.flightontrack.entities.EntityAcft;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.shared.Props;
import com.flightontrack.pilot.Pilot;

import static com.flightontrack.shared.Const.*;
import static shared.AppConfig.pIsNFCcapable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//import shared.AppConfig;

public class AircraftActivity extends Activity {
    private static final String TAG = "AircraftActivity";

    TextView txtBlueText;
    //EditText txtAcftRegNum;
    EditText txtAcftName;
    ArrayAdapter<String> arrayAdapter;
    String[] stringArray= new String[100];
    AutoCompleteTextView autoCompleteTextView;
    TextView txtAcftTagId;
    TextView txtUserName;
    Button doneButton;
    Button cancelButton;
    Button clearButton;
    Switch nfcSwitch;
    //ShowAlertClass showAlertClass;
    protected static NfcAdapter nfcAdapter;
    IntentFilter tagDetected;
    IntentFilter ndefDetected;
    IntentFilter[] nfcFilters;
    PendingIntent pendingIntent;
    ArrayList acftNumArray = new ArrayList<String>();
    ArrayList acftNameArray = new ArrayList<String>();

    public static void clearAcftPreferences() {
        //Log.d.d(TAG, "clearPref()");
        Props.editor.remove("AcftMake").commit();
        Props.editor.remove("AcftModel").commit();
        Props.editor.remove("AcftSeries").commit();
        Props.editor.remove("AcftRegNum").commit();
        Props.editor.remove("AcftTagId").commit();
        Props.editor.remove("AcftName").commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "AircraftActivity onCreate", 'd'));
        setContentView(pIsNFCcapable ? R.layout.activity_acraft : R.layout.activity_acraft_no_nfc);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.acraft, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "AircraftActivity onResume", 'd'));
        txtUserName = findViewById(R.id.txtUserName);
        txtUserName.setText(Pilot.getPilotUserName());
        txtAcftName = findViewById(R.id.txtAcftName);
        autoCompleteTextView = findViewById(R.id.txtAcftRegNum);
        autoCompleteTextView.setThreshold(1);

        doneButton = findViewById(R.id.btn_acft_done);
        cancelButton = findViewById(R.id.btn_acft_cancel);
        clearButton = findViewById(R.id.btn_acft_clear);

        if (pIsNFCcapable) {
            nfcSwitch = findViewById(R.id.switch_nfc);
            txtBlueText = findViewById(R.id.txtBlueText);
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//            txtAcftMake = (TextView) findViewById(R.id.txtAcftMake);
//            txtAcftModel = (TextView) findViewById(R.id.txtAcftModel);
//            txtAcftSeries = (TextView) findViewById(R.id.txtAcftSeries);
//            txtAcftTagId = (TextView) findViewById(R.id.txtAcftTagId);
            setAcft(getAcft());
        }

        String acftSet = Props.sharedPreferences.getString("defaultAcftSet",null);
        if (null != acftSet) try {
            JSONObject jsonDefaultAcftSet = new JSONObject(acftSet);
            autoCompleteTextView.setText(jsonDefaultAcftSet.getString("AcftRegNum"));
            txtAcftName.setText(jsonDefaultAcftSet.getString("AcftName"));
        }
        catch(JSONException e){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "JSONException: JSONObject jsonDefaultAcftSet ", 'e'));
        }

        Set<String> autoCompleteAcftSet = Props.sharedPreferences.getStringSet("autoCompleteAcftSet",null);
        if (null!=autoCompleteAcftSet) {

        for (String acft:autoCompleteAcftSet) {
//            //acft.
            try {
                JSONObject json = new JSONObject(acft);
                String acftNum = json.getString("AcftRegNum");
                acftNumArray.add(acftNum);
                String acftName = json.getString("AcftName");
                acftNameArray.add(acftName);
                //json.getJSONArray(acft);
            }
            catch (JSONException e) {
                        //Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ", e);
                    }
        }
        }
        //if (acftSet!=null) stringArray = Arrays.copyOf(acftSet.toArray(), acftSet.size(),String[].class);
        //if (autoCompleteAcftSet!=null) stringArray = Arrays.copyOf(autoCompleteAcftSet.toArray(), autoCompleteAcftSet.size(),String[].class);
        if (autoCompleteAcftSet!=null) stringArray = (String[]) acftNumArray.toArray(new String[0]);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, stringArray);
        autoCompleteTextView.setAdapter(arrayAdapter);
//        String AcftRegNum = Props.sharedPreferences.getString("AcftRegNum", null);
//        String AcftName = Props.sharedPreferences.getString("AcftName", null);
        init_listeners();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return (id == R.id.action_settings) || super.onOptionsItemSelected(item);
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
    }

    void init_listeners() {

        doneButton.setOnClickListener(view -> {
//            JSONObject json = new JSONObject();
//            try {
////                json.put("AcftRegNum", txtAcftRegNum.getText().toString());
//                json.put("AcftName", txtAcftName.getText().toString());
//                setAcft_nonfc(json);
//                if (pIsNFCcapable) {
////                    json.put("AcftMake", txtAcftMake.getText().toString());
////                    json.put("AcftModel", txtAcftModel.getText().toString());
////                    json.put("AcftSeries", txtAcftSeries.getText().toString());
////                    json.put("AcftTagId", txtAcftTagId.getText().toString());
//                    setAcft(json);
//                }
//            } catch (JSONException e) {
//                //Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ", e);
//            }
            Set<String> acftSet = Props.sharedPreferences.getStringSet("acftSet",null);
            if (null==acftSet) acftSet = new HashSet();
            EntityAcft entityAcft = new EntityAcft(
                    autoCompleteTextView.getText().toString().trim(),
                    txtAcftName.getText().toString().trim()
                    );
            entityAcft.save();

//            String acftNum = autoCompleteTextView.getText().toString();
//            String acftName = txtAcftName.getText().toString();
//            if (null!=acftNum) acftSet.add(acftNum);
//
//            // Put an ArrayList to SharedPreferences
//            Props.editor.putStringSet("acftSet",acftSet);
//            Props.editor.putString("AcftRegNum", acftNum.trim());
//            Props.editor.putString("AcftName", acftName.trim());
//            Props.editor.commit();
            Pilot.setPilotUserName(txtUserName.getText().toString());
            finish();
        });
        cancelButton.setOnClickListener(view -> finish());
        clearButton.setOnClickListener(view -> {
            clearAcftPreferences();
            setAcft_nonfc(getAcft());
            if (pIsNFCcapable) {
                setAcft(getAcft());
            }
        });
        if (pIsNFCcapable) {
            enableNfcForegroundMode();
            nfcSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "AircraftActivity onCheckedChanged", 'd'));
                setTagNFCState(buttonView.isChecked());
            });
        }
//                txtUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                String input;
//                if (actionId == EditorInfo.IME_ACTION_DONE ||
//                        event.getAction() == KeyEvent.ACTION_DOWN &&
//                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER
//                        ) {
//                    input = v.getText().toString();
//                    Util.setUserName(input);
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(txtUserName.getWindowToken(), 0);
//                    return true; // consume.
//                }
//                return false; // pass on to other listeners.
//            }
//        });
        txtUserName.setOnFocusChangeListener((v, hasFocus) -> {
            String input;
            EditText editText;
            if (!hasFocus) {
                editText = (EditText) v;
                input = editText.getText().toString();
                Pilot.setPilotUserName(input);
            }
        });
//        autoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String item = (String) adapterView.getItemAtPosition(i);
//                new FontLogAsync().execute(new EntityLogMessage(TAG, "OnItemSelectedListener "+item, 'd'));
//                //Don't use this method
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                new FontLogAsync().execute(new EntityLogMessage(TAG, "onNothingSelected ", 'd'));
//
//                System.out.println("Nothing selected");
//            }
//        });
        autoCompleteTextView.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //String item = (String) parent.getItemAtPosition(position);

                String acftName = (String) acftNameArray.get(position);
                //new FontLogAsync().execute(new EntityLogMessage(TAG, "OnItemClickListener "+item, 'd'));
                //new FontLogAsync().execute(new EntityLogMessage(TAG, "OnItemClickListener acftName "+acftName, 'd'));
                txtAcftName.setText(acftName);
            }
        });
    }

    void setAcft(JSONObject json) {
        try {
            new FontLogAsync().execute(new EntityLogMessage(TAG, json.toString(), 'd'));
            String AcftMake = json.getString("AcftMake");
            String AcftModel = json.getString("AcftModel").replace(" ", "");
            String AcftSeries = json.getString("AcftSeries").replace(" ", "");
            String AcftRegNum = json.getString("AcftRegNum").replace(" ", "");
            String AcftTagId = json.getString("AcftTagId");
            String AcftName = json.getString("AcftName");
            Props.editor.putString("AcftMake", AcftMake.trim());
            Props.editor.putString("AcftModel", AcftModel.trim());
            Props.editor.putString("AcftSeries", AcftSeries.trim());
            Props.editor.putString("AcftRegNum", AcftRegNum.trim());
            Props.editor.putString("AcftTagId", AcftTagId.trim());
            Props.editor.putString("AcftName", AcftName.trim());
            Props.editor.commit();

            //txtAcftMake.setText(AcftMake);
            //txtAcftModel.setText(AcftModel);
            //txtAcftSeries.setText(AcftSeries);
            autoCompleteTextView.setText(AcftRegNum);
            //txtAcftTagId.setText(AcftTagId);
            txtAcftName.setText(AcftName);

        } catch (JSONException e) {
            Log.e(GLOBALTAG, TAG + "Couldn't parse JSON: ");
        }
    }

    void setAcft_nonfc(JSONObject json) {
        try {
            new FontLogAsync().execute(new EntityLogMessage(TAG, json.toString(), 'd'));

            Set<String> autoCompleteAcftSet = Props.sharedPreferences.getStringSet("autoCompleteAcftSet",null);
            if (null== autoCompleteAcftSet) autoCompleteAcftSet = new HashSet();
            String acftToAdd = autoCompleteTextView.getText().toString();
            if (null!=acftToAdd) autoCompleteAcftSet.add(acftToAdd);

            // Put an ArrayList to SharedPreferences
            Props.editor.putStringSet("autoCompleteAcftSet", autoCompleteAcftSet);
            Props.editor.commit();

            String AcftRegNum = json.getString("AcftRegNum").replace(" ", "");
            String AcftName = json.getString("AcftName");
            Props.editor.putString("AcftRegNum", AcftRegNum.trim());
            Props.editor.putString("AcftName", AcftName.trim());
            Props.editor.commit();

            autoCompleteTextView.setText(AcftRegNum);
            txtAcftName.setText(AcftName);

        } catch (JSONException e) {
            Log.e(GLOBALTAG, TAG + "Couldn't parse JSON: ");
        }
    }

    JSONObject getAcft() {
        //String[] adapterArray= new String[100];
        Set<String> acftSet = Props.sharedPreferences.getStringSet("acftSet",null);
                //getResources().getString(R.string.sp_key_colors),
//                null
//        );
        if (acftSet!=null) stringArray = Arrays.copyOf(acftSet.toArray(), acftSet.size(),String[].class);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, stringArray);
        autoCompleteTextView.setAdapter(arrayAdapter);

        JSONObject json = new JSONObject();
        try {
            json.put("AcftMake", Props.sharedPreferences.getString("AcftMake", ""));
            json.put("AcftModel", Props.sharedPreferences.getString("AcftModel", ""));
            json.put("AcftSeries", Props.sharedPreferences.getString("AcftSeries", ""));
            json.put("AcftRegNum", Props.sharedPreferences.getString("AcftRegNum", getString(R.string.default_acft_N)));
            json.put("AcftTagId", Props.sharedPreferences.getString("AcftTagId", ""));
            json.put("AcftName", Props.sharedPreferences.getString("AcftName", ""));
        } catch (JSONException e) {
            //Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ", e);
        }
        return json;
    }

    public void setTagNFCState(Boolean tagstate){
        if (tagstate && !nfcAdapter.isEnabled()) {
            try (ShowAlertClass showAlertClass=new ShowAlertClass(this)) {
                showAlertClass.showNFCDisabledAlertToUser();
                tagstate = false;
                nfcSwitch.setChecked(tagstate);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        Props.editor.putBoolean("nfctagstate", tagstate).commit();
        txtBlueText.setText(getTagNFCState() ? R.string.instructions1 : R.string.instructions2);
    }

    public static Boolean getTagNFCState() {
        return Props.sharedPreferences.getBoolean("nfctagstate", false);
    }

    public void enableNfcForegroundMode() {
        //Util.appendLog(TAG+ "AircraftActivity enableForegroundMode");
        // foreground mode gives the current active application priority for reading scanned tags
        tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); // filter for tags
        ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("application/com.flightontrack");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Could not add MIME type.", e);
        }
        nfcFilters = new IntentFilter[]{tagDetected, ndefDetected};
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
    }

    NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
                || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }
        } else {
            //Log.e(GLOBALTAG,TAG+ "Unknown intent.");
            finish();
        }
        return msgs;
    }

    @Override
    public void onNewIntent(Intent intent) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "AircraftActivity onNewIntent", 'd'));
        //Util.appendLog(TAG+ intent.getAction());
        if ((intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)
                || intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) && getTagNFCState()) {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
            NdefRecord record = msgs[0].getRecords()[0];
            byte[] payload = record.getPayload();
            //String jsonString = new String(payload);
            try {
                JSONObject j = new JSONObject(new String(payload));
                j.put("AcftName", txtAcftName.getText().toString());
                setAcft(j);
            } catch (JSONException e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "Couldn't create json from NFC: " + e.getMessage(), 'e'));
            }
        }
    }
}
