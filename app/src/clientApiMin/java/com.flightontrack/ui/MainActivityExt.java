package ui;

import android.view.MenuItem;

import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.ui.MainActivity;
import static com.flightontrack.shared.Props.*;

public class MainActivityExt {
    static final String TAG = "MainActivityExt";

    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
//    public void startOnReboot(MainActivity ctx){
//        if (SessionProp.pIsOnReboot && !SessionProp.pIsStartedOnReboot) {
//            ctx.trackingButton.performClick();
//            //SessionProp.pIsStartedOnReboot=true;
//            new FontLogAsync().execute(new EntityLogMessage(TAG, " : performClick", 'd'));
//        }
//}
}

