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
import com.flightontrack.entities.EntityAcftAutoCompleteArray;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.shared.Props;
import com.flightontrack.pilot.Pilot;

import static com.flightontrack.definitions.SHPREF.*;
import static shared.AppConfig.pIsNFCcapable;

import org.json.JSONException;
import org.json.JSONObject;

public class AircraftActivity extends Activity {
    private static final String TAG = "AircraftActivity";

    TextView txtBlueText;
    EditText txtAcftName;
    ArrayAdapter<String> arrayAdapter;
    AutoCompleteTextView txtAutoCompleteAcftNum;
    TextView txtAcftTagId;
    TextView txtUserName;
    Button doneButton;
    Button cancelButton;
    Button clearButton;
    Switch nfcSwitch;
    EntityAcftAutoCompleteArray entityAcftAutoCompleteArray;
    EntityAcft entityAcft;
    //ShowAlertClass showAlertClass;
    protected static NfcAdapter nfcAdapter;
    IntentFilter tagDetected;
    IntentFilter ndefDetected;
    IntentFilter[] nfcFilters;
    PendingIntent pendingIntent;


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
        txtAutoCompleteAcftNum = findViewById(R.id.txtAcftRegNum);
        txtAutoCompleteAcftNum.setThreshold(1);

        doneButton      = findViewById(R.id.btn_acft_done);
        cancelButton    = findViewById(R.id.btn_acft_cancel);
        clearButton     = findViewById(R.id.btn_acft_clear);

        if (pIsNFCcapable) {
            nfcSwitch = findViewById(R.id.switch_nfc);
            txtBlueText = findViewById(R.id.txtBlueText);
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }

        entityAcft = new EntityAcft();
        txtAcftName.setText(entityAcft.AcftName);
        txtAutoCompleteAcftNum.setText(entityAcft.AcftNum);

        entityAcftAutoCompleteArray = new EntityAcftAutoCompleteArray();
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, entityAcftAutoCompleteArray.acftNumArray);
        txtAutoCompleteAcftNum.setAdapter(arrayAdapter);

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
            entityAcft.AcftNum = txtAutoCompleteAcftNum.getText().toString();
            entityAcft.AcftName = txtAcftName.getText().toString();
            entityAcft.save();

            Pilot.setPilotUserName(txtUserName.getText().toString());
            finish();
        });

        cancelButton.setOnClickListener(view -> finish());

        clearButton.setOnClickListener(view -> {
            //clearAcftPreferences();
            txtAutoCompleteAcftNum.setText(null);
            txtAcftName.setText(null);
            //Props.editor.remove("defaultAcftSet").commit();
        });

        txtUserName.setOnFocusChangeListener((v, hasFocus) -> {
            String input;
            EditText editText;
            if (!hasFocus) {
                editText = (EditText) v;
                input = editText.getText().toString();
                Pilot.setPilotUserName(input);
            }
        });

        txtAutoCompleteAcftNum.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                String item = (String) parent.getItemAtPosition(position);
                int i = entityAcftAutoCompleteArray.acftNumArrayList.indexOf(item);
                entityAcftAutoCompleteArray.acftNameArrayList.get(i);
                txtAcftName.setText((String)   entityAcftAutoCompleteArray.acftNameArrayList.get(i));
            }
        });

        if (pIsNFCcapable) {
            enableNfcForegroundMode();
            nfcSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "AircraftActivity onCheckedChanged", 'd'));
                setTagNFCState(buttonView.isChecked());
            });
        }
    }

    public void setTagNFCState(Boolean tagstate){
        if (tagstate && !nfcAdapter.isEnabled()) {
            try (ShowAlertClass showAlertClass=new ShowAlertClass(this)) {
                showAlertClass.showNFCDisabledAlertToUser();
                tagstate = false;
                nfcSwitch.setChecked(tagstate);
            }
            catch(Exception e){
                new FontLogAsync().execute(new EntityLogMessage(TAG, "setTagNFCState -> " + e.getMessage(), 'e',e));
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
            new FontLogAsync().execute(new EntityLogMessage(TAG, "enableNfcForegroundMode -> " + e.getMessage(), 'e',e));
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
            try {
                JSONObject j = new JSONObject(new String(payload));
                new FontLogAsync().execute(new EntityLogMessage(TAG, j.toString(), 'd'));
                entityAcft = new EntityAcft(
                    j.getString(ACFTREGNUM),
                    null,
                    j.getString(ACFTTAGID)
                );
                txtAutoCompleteAcftNum.setText(entityAcft.AcftNum);
                txtAcftName.setText(entityAcft.AcftName);
            } catch (JSONException e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "onNewIntent() Couldn't create json from NFC: " + e.getMessage(), 'e',e));
            }
        }
    }


}
