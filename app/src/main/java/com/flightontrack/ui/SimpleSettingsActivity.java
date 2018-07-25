package com.flightontrack.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;

import shared.AppConfig;
import ui.SimpleSettingsActivityExt;

import static com.flightontrack.shared.Const.MY_PERMISSIONS_RITE_EXTERNAL_STORAGE;
import static com.flightontrack.shared.Props.SessionProp;
import static com.flightontrack.shared.Props.SessionProp.sqlHelper;
import static shared.AppConfig.pIsRelease;

//import static com.flightontrack.shared.Props.AppConfig.pIsRelease;

public class SimpleSettingsActivity extends Activity implements AdapterView.OnItemSelectedListener,EventBus {

    final String TAG = "SimpleSettingsActivityExt";
    TextView txtUser;
    public static TextView txtPsw;
    public static TextView txtBuild;
    TextView txtCached;
    Button resetButton;
    Button clearCacheButton;
    Button sendCacheButton;
    Button getPswButton;
    Spinner spinnerUrls;
    //Spinner spinnerTextTo;
    CheckBox chBoxIsDebug;
    CheckBox chBoxIsRoad;
    ProgressDialog progressBar;
    public static SimpleSettingsActivity simpleSettingsActivityInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleSettingsActivityInstance=this;
        setContentView(R.layout.activity_simple_settings);
        txtBuild= findViewById((R.id.txtBuild));
        txtBuild.setText((getString(R.string.app_label)+" "+ AppConfig.pAppRelease+ AppConfig.pAppReleaseSuffix));
        resetButton = findViewById(R.id.btnReset);
        resetButton.setOnClickListener(view -> {
            SessionProp.resetSessionProp();
            updateUI();
            //spinnerUrls.setSelection(SessionProp.pSpinnerUrlsPos);
            Util.setPsw(null);
            getPswButton.setText(R.string.label_btnpsw_get);
            //MainActivity.spinnerMinSpeed.setSelection(Util.getSpinnerSpeedPos());
        });
        clearCacheButton = findViewById(R.id.btnClearCache);
        clearCacheButton.setOnClickListener(view -> EventBus.distribute(new EventMessage(EVENT.SETTINGACT_BUTTONCLEARCACHE_CLICKED)));
        sendCacheButton = findViewById(R.id.btnSendCache);
        sendCacheButton.setOnClickListener(view -> {
            progressBar.show();
            EventBus.distribute(new EventMessage(EVENT.SETTINGACT_BUTTONSENDCACHE_CLICKED));
        });
        getPswButton = findViewById(R.id.btnGetPsw);
        getPswButton.setOnClickListener(view -> {
            if (Util.getPsw()==null) {
                Util.setCloudPsw(view);
            }
            txtPsw.setVisibility(View.VISIBLE);
        });
//        if(!AppConfig.pIsAppTypePublic) {
//            chBoxIsOnReboot = findViewById(R.id.isOnRebootCheckBox);
//            if (null!=chBoxIsOnReboot) {
//                chBoxIsOnReboot.setChecked(SessionProp.pIsOnReboot);
//                chBoxIsOnReboot.setOnCheckedChangeListener((compoundButton, b) -> {
//                    SessionProp.pIsOnReboot = b;
//                });
//            }
//            spinnerTextTo = findViewById(R.id.spinnerTextTo);
//            ArrayAdapter<CharSequence> adapterTextTo = ArrayAdapter.createFromResource(this,R.array.textto_array, android.R.layout.simple_spinner_item);
//            adapterTextTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinnerTextTo.setAdapter(adapterTextTo);
//            spinnerTextTo.setOnItemSelectedListener(this);
//        }
//        else{
//            findViewById(R.id.layoutTextTo).setVisibility(View.INVISIBLE);
//            findViewById(R.id.layoutStartOnReboot).setVisibility(View.INVISIBLE);
//        }
        chBoxIsDebug = findViewById(R.id.isDebugCheckBoxCheckBox);
        //chBoxIsDebug.setChecked(SessionProp.pIsDebug);
        chBoxIsDebug.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) set_writePermissions();
            SessionProp.pIsDebug=b;
        });
        chBoxIsRoad = findViewById(R.id.isRoadCheckBox);
        //chBoxIsRoad.setChecked(SessionProp.pIsRoad);
        chBoxIsRoad.setOnCheckedChangeListener((compoundButton, b) -> {
            SessionProp.pIsRoad=b;
            //Route.set_isRoad(b);
        });
        txtUser= findViewById((R.id.txtWebsiteUser));
        txtUser.setText(Pilot.getUserID());
        txtPsw= findViewById((R.id.txtWebsitePsw));
        txtPsw.setText(Util.getPsw());
        //chBoxIsDebug.setChecked(Util.getIsDebug());
        spinnerUrls = findViewById(R.id.spinnerUrlId);
        ArrayAdapter<CharSequence> adapterUrl = ArrayAdapter.createFromResource(this,pIsRelease?R.array.url_array_release:R.array.url_array, android.R.layout.simple_spinner_item);
        adapterUrl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrls.setAdapter(adapterUrl);
        spinnerUrls.setOnItemSelectedListener(this);
        txtCached= findViewById((R.id.txtCached));
        txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));

        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage(getString(R.string.progressbar_cachesending));
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        progressBar.setMax(100);
        progressBar.setProgress(100);
        progressBar.setOnCancelListener(dialog -> {
            if (progressBar.isShowing()) {
                progressBar.dismiss();
            }
            finish();
        });
        new SimpleSettingsActivityExt().init(this);
        updateUI();

    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed(); //done to get to recreate (not to create) activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
                super.onBackPressed(); //done to get to recreate (not to create) activity
    }

    public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
        if (parent.getId()==R.id.spinnerUrlId) {
            SessionProp.pSpinnerUrlsPos=pos;
        }
        if (parent.getId()==R.id.spinnerTextTo) {
            SessionProp.pSpinnerTextToPos=pos;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    protected void set_writePermissions() {
        final int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionWrite == PackageManager.PERMISSION_DENIED) {
            chBoxIsDebug.setChecked(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_RITE_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Util.setIsDebug(true);
                    SessionProp.pIsDebug=true;
                    chBoxIsDebug.setChecked(true);
                } else {
                    //Util.setIsDebug(false);
                    //chBoxIsDebug.setChecked(Util.getIsDebug());
                    Toast.makeText(this,R.string.toast_permiss_declined_2, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleSettingsActivityInstance=null;
        SessionProp.save();
    }
    @Override
    public void onStop() {
        super.onStop();
        simpleSettingsActivityInstance=null;
        SessionProp.save();
    }
    @Override
    public void onPause() {
        super.onPause();
        SessionProp.save();
    }

    private void updateUI(){
        spinnerUrls.setSelection(SessionProp.pSpinnerUrlsPos);
        //if(!AppConfig.pIsAppTypePublic) spinnerTextTo.setSelection(SessionProp.pSpinnerTextToPos);
        chBoxIsDebug.setChecked(SessionProp.pIsDebug);
        chBoxIsRoad.setChecked(SessionProp.pIsRoad);
    }
@Override
    public void eventReceiver(EventMessage eventMessage){
    EVENT ev = eventMessage.event;
    new FontLogAsync().execute(new EntityLogMessage(TAG, " eventReceiver : "+ev, 'd'));
        switch(ev){
            case SESSION_ONSENDCACHECOMPLETED:
                txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
                if (progressBar!=null)  progressBar.dismiss();
                break;
            case SQL_ONCLEARCACHE_COMPLETED:
                txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
                break;
            case FLIGHTBASE_GETFLIGHTNUM:
                if (progressBar!=null)  progressBar.dismiss();
                break;
        }
    }
}
