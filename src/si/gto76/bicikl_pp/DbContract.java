package si.gto76.bicikl_pp;

import android.provider.BaseColumns;

public final class DbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DbContract() {}

    /* Inner class that defines the table contents */
    public static abstract class DbStations implements BaseColumns {
        public static final String TABLE_NAME = "stationsAvailability";
        public static final String COLUMN_NAME_STATION_ID = "stationId";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_AVAILABLE = "available";
        public static final String COLUMN_NAME_FREE = "free";
    }
    
    public static abstract class DbOptions implements BaseColumns {
        public static final String TABLE_NAME = "options";
        public static final String COLUMN_NAME_OPTION = "option";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String OPTION_ID_CYCLING_SPEED = "cyclingSpeed";
        public static final String OPTION_ID_ACCEPTABLE_AVAILABILITY = "acceptableAvailability";
    }
}