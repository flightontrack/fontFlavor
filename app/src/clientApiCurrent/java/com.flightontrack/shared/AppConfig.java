package shared;


import android.content.Context;

public final class AppConfig {
    public static String pAppRelease = "1.81";
    public static boolean pIsNFCcapable=true;
    public static boolean pIsNFCEnabled =true;
    public static boolean pIsAppTypePublic=true;
    //public static boolean pAutostart=false;
    public static String pAppReleaseSuffix = "p";
    /// these properties updated dynamically in run time
    public static String pMainActivityLayout = "full";
    public static boolean pIsRelease =false;

    public AppConfig() {
    }
    public AppConfig(Context ctx){
    }
    public static void get(){
        //pIsAppTypePublic = false;
        //pAutostart = false;
        //pIsNFCEnabled = false;
        pIsNFCcapable = false;
    }
    public void unregisterReceivers(Context ctx){
    }
}
