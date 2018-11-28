package shared;


import android.content.Context;
import com.flightontrack.BuildConfig;

public final class AppConfig {
    public static String pAppRelease = BuildConfig.VERSION_NAME;
    public static boolean pIsRelease = BuildConfig.BUILD_TYPE == "release";
    public static boolean pIsAppTypePublic=true;
    public static String pAppReleaseSuffix = "p";
    public static String pMainActivityLayout = "full";
    public static boolean pIsNFCcapable=true;
    public static boolean pIsNFCEnabled =true;

    public AppConfig() {
    }
    public AppConfig(Context ctx){
    }

    public void unregisterReceivers(Context ctx){
    }
}
