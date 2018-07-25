package ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.Props;
import com.flightontrack.ui.MainActivity;

import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Props.mainactivityInstance;

public class MainActivityExt {
    static final String TAG = "MainActivityExt";

    public boolean onOptionsItemSelected(MenuItem item) {
        //Util.setUserName(txtUserName.getText().toString());
        switch (item.getItemId()) {
            case R.id.privpolicy:
                privacyPolicy();
                return true;
            case R.id.action_facebook:
                if (!(RouteBase.activeFlight == null)) facebActivity();
                else
                    Toast.makeText(mainactivityInstance, ctxApp.getString(R.string.start_flight_first), Toast.LENGTH_LONG).show();
                return true;
        }
        return true;
    }

    void privacyPolicy() {
        try {
            Intent intent = new Intent(ctxApp, PrivacyPolicyActivity.class);
            ctxApp.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(ctxApp, "Can't reach help webpage.", Toast.LENGTH_SHORT).show();
        }
    }

    public void facebActivity() {
        Intent intent = new Intent(ctxApp, FaceBookActivity.class);
        ctxApp.startActivity(intent);
    }

    public void startOnReboot(MainActivity ctx){
        /// do nothing
        }
}



