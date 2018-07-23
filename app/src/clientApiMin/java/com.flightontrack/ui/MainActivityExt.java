package ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.ui.FaceBookActivity;
import com.flightontrack.ui.PrivacyPolicyActivity;

import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Props.mainactivityInstance;

public class MainActivityExt {
    static final String TAG = "MainActivity";

    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

}

