package com.flightontrack.definitions;

import static com.flightontrack.definitions.SHPREF.*;
import static com.flightontrack.shared.Props.editor;
import static com.flightontrack.shared.Props.sharedPreferences;

public final class Limits {

    public static final int WAY_POINT_HARD_LIMIT = 1200;
    public static final int LEG_COUNT_HARD_LIMIT = 15;

    public static int getWayPointLimit() {
        return sharedPreferences.getInt(WAYPOINTLIMIT, WAY_POINT_HARD_LIMIT);
    }

    public static void setWayPointLimit(int wp_limit) {
        editor.putInt("WAYPOINTLIMIT", WAY_POINT_HARD_LIMIT > wp_limit ? WAY_POINT_HARD_LIMIT : wp_limit).commit();
    }
}
