package si.gto76.bicikl_pp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.graphics.Color;
import android.location.Location;

public class Util {

	public static Location getLocation(double lat, double lng) {
		Location location = new Location("util");
		location.setLatitude(lat);
		location.setLongitude(lng);
		return location;
	}

	public static Location getLocation(LatLng latLng) {
		Location location = new Location("util");
		location.setLatitude(latLng.latitude);
		location.setLongitude(latLng.longitude);
		return location;
	}

	public static String secondsToText(int seconds) {
		int minutes = 1 + (seconds / 60);
		if (minutes > 59) {
			int hours = minutes / 60;
			int remainingMinutes = minutes % 60;
			return hours + " " + Conf.HOURS_ABR + " " + remainingMinutes + " " + Conf.MINUTES_ABR;
		}
		return minutes + " " + Conf.MINUTES_ABR;
	}

	public static List<LatLng> decodePoly(String encoded) {
		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(position);
		}
		return poly;
	}

	public static String getDate(long milliSeconds, String dateFormat) {
		// Create a DateFormatter object for displaying date in specified
		// format.
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

		// Create a calendar object that will convert the date and time value in
		// milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	public static int getPathColor(Station origin, Station destionation) {
		int weakestLink = Math.min(origin.available, destionation.free);
		if (weakestLink >= Conf.acceptableAvailability) {
			return Color.GREEN;
		} else if (weakestLink > 0) {
			return Color.YELLOW;
		} else {
			return Color.RED;
		}
	}
	
	// still needs to asign value to Conf.cyclingSpeed 
//	public static int getOptionValueFromDb(Context context, String id, int defaultValue) {
//		DbHelper aDbHelper = new DbHelper(context);
//		SQLiteDatabase db = aDbHelper.getReadableDatabase();
//
//		String[] select = { DbOptions.COLUMN_NAME_VALUE };
//		String where = DbOptions.COLUMN_NAME_OPTION + "=?";
//		String[] whereWildcards = { id };
//
//		Cursor cursor = db.query(DbOptions.TABLE_NAME, select, where, whereWildcards, null, null, null);
//		if (cursor.getCount() == 0) {
//			db.close();
//			writeOptionValueToDb(context, id, defaultValue);
//			return defaultValue;
//		} else {
//			cursor.moveToFirst();
//			int value = cursor.getInt(cursor.getColumnIndexOrThrow(DbOptions.COLUMN_NAME_VALUE));
//			db.close();
//			return value;
//		}
//	}
//	
//	// still needs to asign to Conf.cyclingSpeed
//	public static void writeOptionValueToDb(Context context, String id, int value) {
//		DbHelper aDbHelper = new DbHelper(context);
//		SQLiteDatabase db = aDbHelper.getWritableDatabase();
//
//		String where = DbOptions.COLUMN_NAME_OPTION + "=?";
//		String[] whereWildcards = { id };
//		db.delete(DbOptions.TABLE_NAME, where, whereWildcards);
//		
//		ContentValues values = new ContentValues();
//		values.put(DbOptions.COLUMN_NAME_OPTION, id); 
//		values.put(DbOptions.COLUMN_NAME_VALUE, value);
//		db.insert(DbOptions.TABLE_NAME, null, values);
//
//		db.close();
//	}
	
//	public static void getCyclingSpeedFromDb(Context context) {
//		DbHelper aDbHelper = new DbHelper(context);
//		SQLiteDatabase db = aDbHelper.getReadableDatabase();
//
//		String[] select = { DbOptions.COLUMN_NAME_VALUE };
//		String where = DbOptions.COLUMN_NAME_OPTION + "=?";
//		String[] whereWildcards = { DbOptions.ROW_NAME_CYCLING_SPEED };
//
//		Cursor cursor = db.query(DbOptions.TABLE_NAME, select, where, whereWildcards, null, null, null);
//		if (cursor.getCount() == 0) {
//			db.close();
//			writeCyclingSpeedToDb(context, Conf.DEFAULT_CYCLING_SPEED);
//		} else {
//
//			cursor.moveToFirst();
//			int speed = cursor.getInt(cursor.getColumnIndexOrThrow(DbOptions.COLUMN_NAME_VALUE));
//
//			db.close();
//			Conf.cyclingSpeed = speed;
//		}
//	}
//
//	public static void writeCyclingSpeedToDb(Context context, int speed) {
//		Conf.cyclingSpeed = speed; ///// D
//		DbHelper aDbHelper = new DbHelper(context);
//		SQLiteDatabase db = aDbHelper.getWritableDatabase();
//
//		String where = DbOptions.COLUMN_NAME_OPTION + "=?";
//		String[] whereWildcards = { DbOptions.ROW_NAME_CYCLING_SPEED }; ///// P
//		db.delete(DbOptions.TABLE_NAME, where, whereWildcards);
//		
//		ContentValues values = new ContentValues();
//		values.put(DbOptions.COLUMN_NAME_OPTION, DbOptions.ROW_NAME_CYCLING_SPEED); ///// P
//		values.put(DbOptions.COLUMN_NAME_VALUE, speed); ///// G
//		db.insert(DbOptions.TABLE_NAME, null, values);
//
//		db.close();
//	}

}
