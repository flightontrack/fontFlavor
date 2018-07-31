package shared;

import android.content.Context;
import android.content.IntentFilter;

import receiver.AlarmManagerCtrl;
import receiver.ReceiverHealthCheckAlarm;
import receiver.ReceiverBatteryLevel;
import com.flightontrack.shared.Props;

import static com.flightontrack.shared.Const.HEALTHCHECK_BROADCAST_RECEIVER_FILTER;

public final class AppConfig {
    public static String pAppRelease = "1.81";
    public static boolean pIsNFCcapable=false;
    public static boolean pIsNFCEnabled =false;
    public static boolean pIsAppTypePublic=false;
    ///             1. start healthcheckalarmreceiver
    ///             2. aicraft activity layout has no nfc
    ///             3. autostart (request flight) is true
    ///             4. app starts on reboot
    //public static boolean pAutostart= Props.SessionProp.pIsStartedOnReboot;
    public static String pMainActivityLayout = "min";

    public static String pAppReleaseSuffix = "c";
    public static boolean pIsRelease =false;

    ReceiverHealthCheckAlarm alarmReceiver;
    ReceiverBatteryLevel receiverBatteryLevel;

    public AppConfig() {
    }
    public AppConfig(Context ctx){
        alarmReceiver = new ReceiverHealthCheckAlarm();
        ctx.registerReceiver(alarmReceiver, new IntentFilter(HEALTHCHECK_BROADCAST_RECEIVER_FILTER));
        AlarmManagerCtrl.initAlarm();
        AlarmManagerCtrl.setAlarm();
        receiverBatteryLevel = new ReceiverBatteryLevel();
        ctx.registerReceiver(receiverBatteryLevel, new IntentFilter("android.intent.action.BATTERY_LOW"));
        //Props.SessionProp.pIsStartedOnReboot=true;
    }
    public static void get(){
        //pIsAppTypePublic = false;
        //pAutostart = false;
        //pIsNFCEnabled = false;
        pIsNFCcapable = false;
    }

    public void unregisterReceivers(Context ctx){
        if (alarmReceiver != null) {
            ctx.unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }
        if (receiverBatteryLevel != null) {
            ctx.unregisterReceiver(receiverBatteryLevel);
            receiverBatteryLevel = null;
        }
    }
}
