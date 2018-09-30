package other;

import com.flightontrack.entities.EntityFlightTimeMessage;

public class Talk implements AutoCloseable{
    static final String TAG = "Talk";

    public Talk (EntityFlightTimeMessage msgobj) {
        /// this class just a placeholder
    }

    @Override
    public void close() throws Exception {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}
