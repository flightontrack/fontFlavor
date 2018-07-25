package ui;


import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.flightontrack.R;
import com.flightontrack.ui.SimpleSettingsActivity;
import static com.flightontrack.shared.Props.*;

public class SimpleSettingsActivityExt{

    final String TAG = "SimpleSettingsActivityExt";

    Spinner spinnerTextTo;
    CheckBox chBoxIsOnReboot;

    public void init(SimpleSettingsActivity ctx) {
    chBoxIsOnReboot = ctx.findViewById(R.id.isOnRebootCheckBox);
            if (null!=chBoxIsOnReboot) {
                chBoxIsOnReboot.setChecked(SessionProp.pIsOnReboot);
                chBoxIsOnReboot.setOnCheckedChangeListener((compoundButton, b) -> {
                    SessionProp.pIsOnReboot = b;
                });
            }
            spinnerTextTo = ctx.findViewById(R.id.spinnerTextTo);
            ArrayAdapter<CharSequence> adapterTextTo = ArrayAdapter.createFromResource(ctxApp,R.array.textto_array, android.R.layout.simple_spinner_item);
            adapterTextTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTextTo.setAdapter(adapterTextTo);
            spinnerTextTo.setOnItemSelectedListener(ctx);
}
}
