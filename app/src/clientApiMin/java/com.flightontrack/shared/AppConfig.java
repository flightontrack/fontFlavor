package shared;

import android.content.Context;
import android.content.IntentFilter;

import receiver.AlarmManagerCtrl;
import receiver.ReceiverHealthCheckAlarm;
import receiver.ReceiverBatteryLevel;
import com.flightontrack.BuildConfig;

import static com.flightontrack.definitions.Finals.HEALTHCHECK_BROADCAST_RECEIVER_FILTER;

public final class AppConfig {
    ///             1. start healthcheckalarmreceiver
    ///             2. aicraft activity layout has no nfc
    ///             3. autostart (request flight) is true
    ///             4. app starts on reboot
    public static String pAppRelease = BuildConfig.VERSION_NAME;
    public static boolean pIsRelease = BuildConfig.BUILD_TYPE == "release";
    public static boolean pIsAppTypePublic=false;
    public static String pAppReleaseSuffix = "c";
    public static String pMainActivityLayout = "min";
    public static boolean pIsNFCcapable=false;
    public static boolean pIsNFCEnabled =false;

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
