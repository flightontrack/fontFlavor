package ui;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.flight.RouteBase;

import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Props.mainactivityInstance;

public class MainActivityExt {
    static final String TAG = "MainActivityExt";

    public Intent onOptionsItemSelected(MenuItem item) {
        //Util.setUserName(txtUserName.getText().toString());
        Intent intent = new Intent();
        try {
            switch (item.getItemId()) {
                case R.id.privpolicy:
                    intent = new Intent(ctxApp, PrivacyPolicyActivity.class);
                    break;
                case R.id.action_facebook:
                    if (!(RouteBase.activeFlight == null)) {
                        intent = new Intent(ctxApp, FaceBookActivity.class);
                    } else
                        Toast.makeText(mainactivityInstance, ctxApp.getString(R.string.start_flight_first), Toast.LENGTH_LONG).show();
                    break;
                case R.id.action_browser:
                    intent = new Intent(ctxApp, FlightOnTrackActivity.class);
                    break;
            }
        }
        catch (Exception e){
            /// do nothing
        }
        return intent;
    }

}



