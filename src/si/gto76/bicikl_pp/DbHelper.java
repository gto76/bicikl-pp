package si.gto76.bicikl_pp;

import si.gto76.bicikl_pp.DbContract.Db;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public static final String COMMA_SEP = ",";
	public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + Db.TABLE_NAME + " ("
			+ Db.COLUMN_NAME_STATION_ID + " TEXT NOT NULL," + Db.COLUMN_NAME_TIME + " LONG NOT NULL,"
			+ Db.COLUMN_NAME_AVAILABLE + " INTEGER NOT NULL," + Db.COLUMN_NAME_FREE + " INTEGER NOT NULL"
			+ " )";

	public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Db.TABLE_NAME;

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 4;
	public static final String DATABASE_NAME = "FeedReader.db";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}