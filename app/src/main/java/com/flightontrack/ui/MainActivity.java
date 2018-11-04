package com.flightontrack.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
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

import com.flightontrack.objects.Aircraft;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
//import receiver.AlarmManagerCtrl;
import com.flightontrack.R;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.objects.MyPhone;
import com.flightontrack.objects.Pilot;
//import com.flightontrack.receiver.ReceiverHealthCheckAlarm;
//import com.flightontrack.receiver.ReceiverBatteryLevel;
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
//import com.google.android.gms.common.api.GoogleApiClient;

import shared.AppConfig;
import ui.MainActivityExt;
//import SimpleSettingsActivity;
//import static shared.AppConfig.*;

import static com.flightontrack.definitions.Finals.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.flight.Session.*;

public class MainActivity extends AppCompatActivity implements EventBus {
    static final String TAG = "MainActivity";

    static boolean  isToDestroy = true;
    TextView        txtAcftNum;
    Button          trackingButton;
    TextView        txtUserName;
    TextView        txtCached;
    CheckBox        chBoxIsMultiLeg;
    ActionMenuView  bottomMenu;
    View            cardLayout1;
    public Spinner  spinnerUpdFreq;
    public Spinner  spinnerMinSpeed;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    public MainActivity() {
    }

    public static Boolean isMainActivityExist() {
        return mainactivityInstance != null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        try {
            //Log.d(TAG, "MainActivityThread:" + Thread.currentThread().getId());
            initProp(getApplicationContext(), this);
            setContentView(R.layout.activity_main);

            txtAcftNum      = findViewById(R.id.txtAcftNum);
            txtUserName     = findViewById(R.id.txtUserName);
            spinnerUpdFreq  = findViewById(R.id.spinnerId);
            spinnerMinSpeed = findViewById(R.id.spinnerMinSpeedId);
            trackingButton  = findViewById(R.id.btnTracking);
            txtCached       = findViewById((R.id.txtCached));

            chBoxIsMultiLeg = findViewById(R.id.patternCheckBox);
            chBoxIsMultiLeg.setOnCheckedChangeListener((compoundButton, b) -> {
                EventBus.distribute(new EventMessage(EVENT.MACT_MULTILEG_ONCLICK).setEventMessageValueBool(chBoxIsMultiLeg.isChecked()));
            });

            //toolbarTop = findViewById(R.id.toolbar_top);
            setSupportActionBar(findViewById(R.id.toolbar_top));
            getSupportActionBar().setTitle(getString(R.string.app_label));

            AppConfig.pMainActivityLayout = findViewById(R.id.TagView).getTag().toString();
            if (AppConfig.pMainActivityLayout.equals("full")) {
                bottomMenu = findViewById(R.id.bottomMenu);
                bottomMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return onOptionsItemSelected(menuItem);
                    }
                });
            }

            cardLayout1 = findViewById(R.id.cardLayoutId1);
            cardLayout1.setOnClickListener(v-> {
                Intent intent = new Intent(ctxApp, AircraftActivity.class);
                startActivity(intent);
            });

            //AppConfig.get();
            AppConfig.pIsNFCcapable = AppConfig.pIsNFCEnabled && isNFCcapable();
            SessionProp.get();
            if (!SessionProp.pIsActivityFinished) {
                /// if previous session crashed reset session prop
                clearOnDestroy();
                SessionProp.get();
            }

            new AppConfig(this);
            updFreqSpinnerSetup();
            minSpeedSpinnerSetup();
            SessionProp.set_isMultileg(true);
            SessionProp.save();

        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "EXCEPTION!!!!: " + e.toString(), 'e'));
            finishActivity();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onResume", 'd'));
        super.onResume();

        SessionProp.get();
        txtAcftNum.setText(new Aircraft().AcftNum);
        BigButton.setTrackingButton(trackingButtonState);
        txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));

        //init_listeners();

        int permissionCheckPhone = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheckLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheckLocation == PackageManager.PERMISSION_DENIED) {
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.putExtra("PERMISSIONTYPE", Manifest.permission.ACCESS_FINE_LOCATION);
            startActivityForResult(intent, START_ACTIVITY_RESULT);
        } else if (permissionCheckPhone == PackageManager.PERMISSION_DENIED) {
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.putExtra("PERMISSIONTYPE", Manifest.permission.READ_PHONE_STATE);
            startActivityForResult(intent, START_ACTIVITY_RESULT);
        } else txtUserName.setText(Pilot.getPilotUserName());


        if (SessionProp.pIsOnReboot && trackingButtonState==BUTTONREQUEST.BUTTON_STATE_RED) {
            trackingButton.performClick();
//            new FontLogAsync().execute(new EntityLogMessage(TAG, " : performClick", 'd'));
        }
        //new MainActivityExt().startOnReboot(this);
        isToDestroy = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getSupportActionBar().setDisplayShowTitleEnabled(true);
        getMenuInflater().inflate(R.menu.menu_top, menu);
        if (AppConfig.pMainActivityLayout.equals("full")) {
            getMenuInflater().inflate(R.menu.menu_bottom, bottomMenu.getMenu());
        }
        //toolbarTop.inflateMenu(R.menu.menu_top);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Util.setUserName(txtUserName.getText().toString());
        switch (item.getItemId()) {
            case R.id.action_help:
                helpPage();
                return true;
            case R.id.action_email:
                sendEmail();
                return true;
            case R.id.action_logbook:
                logBookPage();
                return true;
            case R.id.action_settings:
                settingsActivity();
                return true;
            case R.id.action_aircraft:
                acftActivity();
                return true;
            default:
                return (new MainActivityExt().onOptionsItemSelected(item));
                //return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        new FontLogAsync().execute(new EntityLogMessage(TAG, "isToDestroy :" + isToDestroy, 'd'));
        if (isToDestroy) {
            new ShowAlertClass(this).showBackPressed();
        } else {
            /// set this flag to true to stop unregister a healthcheck in onDestroy and do not set ctxApp to null
            //isToDestroy = true;
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("MyStringInstanceState", "Activity Recreation");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "OnDestroy", 'd'));
        SessionProp.save();
        SessionProp.clearOnDestroy();
        if (isToDestroy) {
            new AppConfig().unregisterReceivers(this);
//            unregisterReceiver(alarmReceiver);
//            alarmReceiver = null;
            ctxApp = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == START_ACTIVITY_RESULT) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.connect();
        //AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onPause() {
        super.onPause();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onPause", 'd'));
        SessionProp.save();
    }

    @Override
    public void onStop() {
        super.onStop();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onStop", 'd'));
        SessionProp.save();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //AppIndex.AppIndexApi.end(client, getIndexApiAction());
        //client.disconnect();
    }

    void init_listeners() {

//            //Crashlytics.getInstance().crash(); // Force a crash
//            //throw new NullPointerException();
//        });

        chBoxIsMultiLeg.setOnCheckedChangeListener((compoundButton, b) -> {
            EventBus.distribute(new EventMessage(EVENT.MACT_MULTILEG_ONCLICK).setEventMessageValueBool(chBoxIsMultiLeg.isChecked()));
        });
    }

    public void  trackingButtonOnClick(View v){
        switch (trackingButtonState) {
            case BUTTON_STATE_RED:
                //Util.setAcftNum(txtAcftNum.getText().toString());
                //TODO pAutostart  need to be replaced with pIsStartedOnReboot
                //TODO ???? что это
                //if (!AppConfig.pAutostart && !is_services_available()) return;
                if (!isAircraftPopulated() && !SessionProp.pIsEmptyAcftOk) {

                    new ShowAlertClass(mainactivityInstance).showAircraftIsEmptyAlert();
                    if (!SessionProp.pIsEmptyAcftOk) return;
                }
                /// signal to start a new route and load a new flight in route flight list
                EventBus.distribute(new EventMessage(EVENT.MACT_BIGBUTTON_ONCLICK_START));
                break;
            default:
                EventBus.distribute(new EventMessage(EVENT.MACT_BIGBUTTON_ONCLICK_STOP));
                break;
        }
    }
    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private boolean isAircraftPopulated() {
        return !(txtAcftNum.getText().toString().trim().equals(getString(R.string.default_acft_N)));
    }

    private boolean isNFCcapable() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        return (nfcAdapter != null);
    }

    public void acftActivity() {
        Intent intent = new Intent(this, AircraftActivity.class);
        startActivity(intent);
    }

//    public void facebActivity() {
//        Intent intent = new Intent(this, FaceBookActivity.class);
//        startActivity(intent);
//    }

    void helpPage() {
        try {
            Intent intent = new Intent(ctxApp, HelpPageActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(ctxApp, "Can't reach help webpage.", Toast.LENGTH_SHORT).show();
        }
    }
//    void privacyPolicy() {
//        try {
//            Intent intent = new Intent(ctxApp, PrivacyPolicyActivity.class);
//            startActivity(intent);
//        } catch (ActivityNotFoundException ex) {
//            Toast.makeText(ctxApp, "Can't reach help webpage.", Toast.LENGTH_SHORT).show();
//        }
//    }

    void logBookPage() {
        try {
            Intent intent = new Intent(ctxApp, LogBookActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "Can't start LogBook Activity", Toast.LENGTH_SHORT).show();
        }
    }

    void sendEmail() {

        MyPhone myPhone = new MyPhone();
        String[] TO = {getString(R.string.email_crash)};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Flight On Track issue");
        String emailText = "APP_BUILD : " + MyPhone.getVersionCode() + '\n' +
                "ANDROID_VERSION : " + myPhone.getMyAndroidVersion() + '\n' +
                "PHONE_MODEL : " +
                //Util.deviceMmnufacturer.toUpperCase()+'\n'+
                //Util.deviceBrand.toUpperCase()+'\n'+
                MyPhone.deviceModel.toUpperCase() + ' ' +
                MyPhone.deviceProduct.toUpperCase() + '\n' +
                "USER : " + Pilot.getUserID() + '\n';

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText + '\n' + getString(R.string.email_commment) + '\n');
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, getString(R.string.email_notinstalled), Toast.LENGTH_SHORT).show();
        }
    }

    public void settingsActivity() {
        try {
            Intent intent = new Intent(ctxApp, SimpleSettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(ctxApp,
                    "Can't start Settings.", Toast.LENGTH_SHORT).show();
        }
    }

    void updFreqSpinnerSetup() {
        String[] interval_name = getResources().getStringArray(R.array.intervalname_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, interval_name);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerUpdFreq.setAdapter(adapter);
        spinnerUpdFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                set_pIntervalLocationUpdateSecPos(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerUpdFreq.setSelection(SessionProp.pIntervalSelectedItem);
    }

    void minSpeedSpinnerSetup() {
        ArrayAdapter<CharSequence> adapterSpeed = ArrayAdapter.createFromResource(this, R.array.speed_array, android.R.layout.simple_spinner_item);
        adapterSpeed.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerMinSpeed.setAdapter(adapterSpeed);
        spinnerMinSpeed.setSelection(SessionProp.pSpinnerMinSpeedPos);
        spinnerMinSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SessionProp.set_pSpinnerMinSpeedPos(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //pMinSpeedArray= getResources().getStringArray(R.array.speed_array);
    }

    public void finishActivity() {
        if (SvcLocationClock.isInstanceCreated()) SvcLocationClock.instanceSvcLocationClock.stopServiceSelf();
        new AppConfig().unregisterReceivers(this);
//        if (alarmReceiver != null) {
//            unregisterReceiver(alarmReceiver);
//            alarmReceiver = null;
//        }
//        if (receiverBatteryLevel != null) {
//            unregisterReceiver(receiverBatteryLevel);
//            receiverBatteryLevel = null;
//        }
        //ctxApp = null;
        txtUserName = null;
        txtAcftNum = null;
        spinnerUpdFreq = null;
        spinnerMinSpeed = null;
        chBoxIsMultiLeg = null;
        trackingButton = null;
        BigButton.bigButtonInstance = null;
        mainactivityInstance = null;
        finish();
    }

    private boolean is_services_available() {

        if (!isGPSEnabled()) {
            new ShowAlertClass(this).showGPSDisabledAlertToUser();
            return false;
        }
        if (!isNetworkAvailable()) {
            new ShowAlertClass(this).showNetworkDisabledAlertToUser();
            return false;
        }
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("Main Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }

    @Override
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, "eventReceiver : " + ev, 'd'));
        //txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
        switch (ev) {
            case PROP_CHANGED_MULTILEG:
                chBoxIsMultiLeg.setChecked(eventMessage.eventMessageValueBool);
                break;
            case SESSION_ONSUCCESS_EXCEPTION:
                Toast.makeText(mainactivityInstance, R.string.toast_server_error, Toast.LENGTH_LONG).show();
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
                break;
            case SESSION_ONSENDCACHECOMPLETED:
                txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
                break;
            case HEALTHCHECK_ONRESTART:
                trackingButton.performClick();
                break;
        }
    }
}

