package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

abstract class StationsLookUp extends LookUp {

	abstract void onSuccessfulFetch(JSONObject result) throws JSONException;

	public StationsLookUp(Context context) {
		super(context, "https://prevoz.org/api/bicikelj/list/");
	}

	// /////

	@Override
	protected void onPostExecute(JSONObject result) {
		if (result == null) {
			Toast.makeText(context, "Error occured while downloading stations data.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		try {
			onSuccessfulFetch(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// ///// GETERS ////////
	
//	protected static List<Station> getStations(JSONObject result) throws JSONException {
//		List<Station> stations = new ArrayList<Station>();
//		for (String id: getIds(result)) {
//			Station station = getStationObject(result, id);
//			stations.add(station);
//		}
//		return stations;
//	}
	
	///////

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

	protected static String getName(JSONObject result, String id) throws JSONException {
		JSONObject station = getStation(result, id);
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
		JSONObject station = getStation(result, id);
		double lat = station.getDouble("lat");
		double lng = station.getDouble("lng");
		return Util.getLocation(lat, lng);
	}

	protected static LatLng getLatLng(JSONObject result, String id) throws JSONException {
		JSONObject station = getStation(result, id);
		double lat = station.getDouble("lat");
		double lng = station.getDouble("lng");
		LatLng latLng = new LatLng(lat, lng);
		return latLng;
	}

	protected static Station getStationObject(JSONObject result, String id) throws JSONException {
		String name = getName(result, id);
		Location location = getLocation(result, id);
		int available = getAvailableBikes(result, id);
		int free = getFreeSpots(result, id);
		return new Station(id, name, location, available, free);
	}

	protected static Bundle getBundle(JSONObject result, String id) throws JSONException {
		Station station = getStationObject(result, id);
		return station.getBundle();
	}

	// ///// PRIVATE ///////

	private static JSONObject getAvailability(JSONObject result, String id) throws JSONException {
		JSONObject station = getStation(result, id);
		return station.getJSONObject("station");
	}

	private static JSONObject getStation(JSONObject result, String id) throws JSONException {
		JSONObject markers = result.getJSONObject("markers");
		return markers.getJSONObject(id);
	}
}
