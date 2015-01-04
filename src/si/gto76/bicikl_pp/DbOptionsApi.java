package si.gto76.bicikl_pp;

import si.gto76.bicikl_pp.DbContract.DbOptions;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbOptionsApi {

	public static void fetchConfigurationFromDb(Context context) {
		int cyclingSpeed = getOptionValueFromDb(context, DbOptions.OPTION_ID_CYCLING_SPEED,
				Conf.DEFAULT_CYCLING_SPEED);
		Conf.cyclingSpeed = cyclingSpeed;
		int acceptableAvailability = getOptionValueFromDb(context, DbOptions.OPTION_ID_ACCEPTABLE_AVAILABILITY,
				Conf.ACCEPTABLE_AVAILABILITY);
		Conf.acceptableAvailability = acceptableAvailability;
	}

	public static int getOptionValueFromDb(Context context, String id, int defaultValue) {
		DbHelper aDbHelper = new DbHelper(context);
		SQLiteDatabase db = aDbHelper.getReadableDatabase();

		String[] select = { DbOptions.COLUMN_NAME_VALUE };
		String where = DbOptions.COLUMN_NAME_OPTION + "=?";
		String[] whereWildcards = { id };

		Cursor cursor = db.query(DbOptions.TABLE_NAME, select, where, whereWildcards, null, null, null);
		if (cursor.getCount() == 0) {
			db.close();
			writeOptionValueToDb(context, id, defaultValue);
			return defaultValue;
		} else {
			cursor.moveToFirst();
			int value = cursor.getInt(cursor.getColumnIndexOrThrow(DbOptions.COLUMN_NAME_VALUE));
			db.close();
			return value;
		}
	}

	public static void writeOptionValueToDb(Context context, String id, int value) {
		DbHelper aDbHelper = new DbHelper(context);
		SQLiteDatabase db = aDbHelper.getWritableDatabase();

		String where = DbOptions.COLUMN_NAME_OPTION + "=?";
		String[] whereWildcards = { id };
		db.delete(DbOptions.TABLE_NAME, where, whereWildcards);

		ContentValues values = new ContentValues();
		values.put(DbOptions.COLUMN_NAME_OPTION, id);
		values.put(DbOptions.COLUMN_NAME_VALUE, value);
		db.insert(DbOptions.TABLE_NAME, null, values);

		db.close();
	}

}
