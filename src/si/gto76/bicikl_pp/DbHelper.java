package si.gto76.bicikl_pp;

import si.gto76.bicikl_pp.DbContract.DbOptions;
import si.gto76.bicikl_pp.DbContract.DbStations;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 6;
	public static final String DATABASE_NAME = "FeedReader.db";

	public static final String COMMA_SEP = ",";
	public static final String SQL_CREATE_STATIONS = "CREATE TABLE " + DbStations.TABLE_NAME + " ("
			+ DbStations.COLUMN_NAME_STATION_ID + " TEXT NOT NULL," + DbStations.COLUMN_NAME_TIME + " LONG NOT NULL,"
			+ DbStations.COLUMN_NAME_AVAILABLE + " INTEGER NOT NULL," + DbStations.COLUMN_NAME_FREE + " INTEGER NOT NULL"
			+ " )";

	public static final String SQL_CREATE_OPTIONS = "CREATE TABLE " + DbOptions.TABLE_NAME + " ("
			+ DbOptions.COLUMN_NAME_OPTION + " TEXT PRIMARY KEY," + DbOptions.COLUMN_NAME_VALUE + " INTEGER NOT NULL"
			+ " )";

	public static final String SQL_DELETE_STATIONS = "DROP TABLE IF EXISTS " + DbStations.TABLE_NAME;
	public static final String SQL_DELETE_OPTIONS = "DROP TABLE IF EXISTS " + DbOptions.TABLE_NAME;


	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_STATIONS);
		db.execSQL(SQL_CREATE_OPTIONS);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_STATIONS);
		db.execSQL(SQL_DELETE_OPTIONS);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}