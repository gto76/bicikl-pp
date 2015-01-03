package si.gto76.bicikl_pp.asynctasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.Conf;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

/**
 * Used for getting info from Google directions API.
 * Its execute method needs four parameters: lat/lng of origin and lat/lng of destination.
 * You have to define onSuccessfulFetch method, that gets the JSON result when it arrives.
 */
public abstract class DurationLookUp extends LookUp {

	public abstract void onSuccessfulFetch(JSONObject result) throws JSONException;

	public DurationLookUp(Context ctx) {
		super(ctx, "https://maps.google.com/maps/api/directions/json");
	}

	// /////

	@Override
	protected JSONObject doInBackground(String... params) {
		String query = "?origin=" + params[0] + "," + params[1] + "&destination=" + params[2] + ","
				+ params[3] + "&sensor=false&mode=walking" + Conf.KEY;
		return super.doInBackground(query);
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if (result == null) {
			Toast.makeText(context, "Error occured while downloading directions data.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		try {
			onSuccessfulFetch(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// /////

	protected static String getDurationText(JSONObject result) throws JSONException {
		JSONObject duration = getDurationObject(result);
		return duration.getString("text");
	}

	protected static int getDurationSeconds(JSONObject result) throws JSONException {
		JSONObject duration = getDurationObject(result);
		return duration.getInt("value");
	}

	protected static int getDistanceMeters(JSONObject result) throws JSONException {
		JSONObject distance = getDistanceObject(result);
		return distance.getInt("value");
	}

	protected static String getPolyline(JSONObject result) throws JSONException {
		JSONArray routes = result.getJSONArray("routes");
		JSONObject overview_polyline = routes.getJSONObject(0).getJSONObject("overview_polyline");
		return overview_polyline.getString("points");
	}
	
	protected static LatLngBounds getBounds(JSONObject result) throws JSONException {
		JSONArray routes = result.getJSONArray("routes");
		JSONObject bounds = routes.getJSONObject(0).getJSONObject("bounds");
		JSONObject southwest = bounds.getJSONObject("southwest");
		double swLat = southwest.getDouble("lat");
		double swLng = southwest.getDouble("lng");
		JSONObject northeast = bounds.getJSONObject("northeast");
		double neLat = northeast.getDouble("lat");
		double neLng = northeast.getDouble("lng");
		return new LatLngBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng));
	}

	// //////

	private static JSONObject getDurationObject(JSONObject result) throws JSONException {
		JSONArray routes = result.getJSONArray("routes");
		JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
		return legs.getJSONObject(0).getJSONObject("duration");
	}

	private static JSONObject getDistanceObject(JSONObject result) throws JSONException {
		JSONArray routes = result.getJSONArray("routes");
		JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
		return legs.getJSONObject(0).getJSONObject("distance");
	}

	// // UTIL

	/**
	 * Converts two locations in string array of length 4, containing their lats
	 * and lngs. Useful when calling execute.
	 */
	public static String[] getVarArgs(Location loc1, Location loc2) {
		String originLat = String.valueOf(loc1.getLatitude());
		String originLng = String.valueOf(loc1.getLongitude());
		String destLat = String.valueOf(loc2.getLatitude());
		String destLng = String.valueOf(loc2.getLongitude());
		String[] out = new String[] { originLat, originLng, destLat, destLng };
		return out;
	}
}