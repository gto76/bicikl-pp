package si.gto76.bicikl_pp.asynctasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.DbHelper;
import si.gto76.bicikl_pp.Station;
import si.gto76.bicikl_pp.Util;
import si.gto76.bicikl_pp.DbContract.DbStations;

import com.google.android.gms.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Used for getting info from Ljubljana public bicycle API. Its execute method doesen't need any parameters.
 * You have to define onSuccessfulFetch method, that gets the JSON result when it arrives.
 * This class also automatically updates the database on every execute call.
 * The database consist of one table containing station id, timestamp, available bikes and free spots.
 */
public abstract class StationsLookUp extends LookUp {

	public abstract void onSuccessfulFetch(JSONObject result) throws JSONException;

	public StationsLookUp(Context context) {
		super(context, "https://prevoz.org/api/bicikelj/list/");
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if (result == null) {
			Toast.makeText(context, "Error occured while downloading stations data.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		try {
			onSuccessfulFetch(result);
			putInDb(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// /////////////////////
	// ///// GETERS ////////
	// /////////////////////

	protected static List<Station> getStations(JSONObject result) throws JSONException {
		List<Station> stations = new ArrayList<Station>();
		for (String id : getIds(result)) {
			Station station = getStation(result, id);
			stations.add(station);
		}
		return stations;
	}

	protected static List<String> getIds(JSONObject result) throws JSONException {
		List<String> ids = new ArrayList<String>();
		JSONObject markers = result.getJSONObject("markers");
		Iterator<String> iter = markers.keys();
		while (iter.hasNext()) {
			String id = iter.next();
			ids.add(id);
		}
		return ids;
	}

	// /////

	protected static String getName(JSONObject result, String id) throws JSONException {
		JSONObject station = getStationJsonObj(result, id);
		return station.getString("name");
	}

	protected static int getAvailableBikes(JSONObject result, String id) throws JSONException {
		JSONObject availability = getAvailability(result, id);
		return availability.getInt("available");
	}

	protected static int getFreeSpots(JSONObject result, String id) throws JSONException {
		JSONObject availability = getAvailability(result, id);
		return availability.getInt("free");
	}

	protected static Location getLocation(JSONObject result, String id) throws JSONException {
		JSONObject station = getStationJsonObj(result, id);
		double lat = station.getDouble("lat");
		double lng = station.getDouble("lng");
		return Util.getLocation(lat, lng);
	}

	protected static LatLng getLatLng(JSONObject result, String id) throws JSONException {
		JSONObject station = getStationJsonObj(result, id);
		double lat = station.getDouble("lat");
		double lng = station.getDouble("lng");
		LatLng latLng = new LatLng(lat, lng);
		return latLng;
	}

	protected static Station getStation(JSONObject result, String id) throws JSONException {
		String name = getName(result, id);
		Location location = getLocation(result, id);
		int available = getAvailableBikes(result, id);
		int free = getFreeSpots(result, id);
		return new Station(id, name, location, available, free);
	}

	public static Bundle getBundle(JSONObject result, String id) throws JSONException {
		Station station = getStation(result, id);
		return station.getBundle();
	}

	// /////

	private static JSONObject getAvailability(JSONObject result, String id) throws JSONException {
		JSONObject station = getStationJsonObj(result, id);
		return station.getJSONObject("station");
	}

	private static JSONObject getStationJsonObj(JSONObject result, String id) throws JSONException {
		JSONObject markers = result.getJSONObject("markers");
		return markers.getJSONObject(id);
	}

	// /////////////////////
	// /////// DB //////////
	// /////////////////////

	private void putInDb(JSONObject result) throws JSONException {
		DbHelper aDbHelper = new DbHelper(context);

		// Gets the data repository in write mode
		SQLiteDatabase db = aDbHelper.getWritableDatabase();

		for (Station station : getStations(result)) {
			if (dbNotUpToDate(db, station)) {
				insertRow(db, station);
			}
		}

		db.close();
	}

	/**
	 * Checks if number of free spots of the station matches the latest record in db.
	 */
	private static boolean dbNotUpToDate(SQLiteDatabase db, Station station) {
		// Get latest stations record
		/* SELECT available FROM table WHERE id = id and time = (SELECT max(time) FROM table WHERE id = id) */
		Cursor cursor = db.rawQuery("SELECT " + DbStations.COLUMN_NAME_AVAILABLE + " FROM " + DbStations.TABLE_NAME
				+ " WHERE " + DbStations.COLUMN_NAME_STATION_ID + " = " + station.id + " AND " + DbStations.COLUMN_NAME_TIME
				+ " = (SELECT MAX(" + DbStations.COLUMN_NAME_TIME + ") FROM " + DbStations.TABLE_NAME + " WHERE "
				+ DbStations.COLUMN_NAME_STATION_ID + " = " + station.id + ")", null);

		if (cursor.getCount() == 0) {
			return true;
		}

		cursor.moveToFirst();
		int available = cursor.getInt(cursor.getColumnIndexOrThrow(DbStations.COLUMN_NAME_AVAILABLE));
		return (available != station.available);
	}

	private static void insertRow(SQLiteDatabase db, Station station) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbStations.COLUMN_NAME_STATION_ID, station.id);
		values.put(DbStations.COLUMN_NAME_TIME, System.currentTimeMillis());
		values.put(DbStations.COLUMN_NAME_AVAILABLE, station.available);
		values.put(DbStations.COLUMN_NAME_FREE, station.free);
		// Insert the new row, returning the primary key value of the new row
		db.insert(DbStations.TABLE_NAME, null, values);
	}

}
