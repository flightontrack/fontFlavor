package ui;

import android.view.View;
import android.widget.AdapterView;

import com.flightontrack.R;
import com.flightontrack.shared.Props;
import com.flightontrack.ui.SimpleSettingsActivity;
public class SimpleSettingsActivityExt {

    public void init(SimpleSettingsActivity ctx) {
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId()== R.id.spinnerUrlId) {
            Props.SessionProp.pSpinnerUrlsPos=pos;
        }
    }

}
