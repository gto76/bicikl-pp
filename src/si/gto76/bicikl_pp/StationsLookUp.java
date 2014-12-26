package si.gto76.bicikl_pp;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

abstract class StationsLookUp extends LookUp {

	public StationsLookUp(Context context) {
		super(context, "https://prevoz.org/api/bicikelj/list/");
	}
	
	///////
	
	protected static String getStationName(JSONObject result, String id) throws JSONException {
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
		Location location = new Location("station");
		location.setLatitude(lat);
		location.setLongitude(lng);
		return location;
	}
	
	///////
	
	private static JSONObject getAvailability(JSONObject result, String id) throws JSONException {
		JSONObject station = getStation(result, id);
		return station.getJSONObject("station");
	}
	
	private static JSONObject getStation(JSONObject result, String id) throws JSONException {
		JSONObject markers = result.getJSONObject("markers");
		return markers.getJSONObject(id);
	}
	
	
}
