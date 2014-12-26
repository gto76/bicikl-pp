package si.gto76.bicikl_pp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

abstract class DurationLookUp extends LookUp {

	public DurationLookUp(Context ctx) {
		super(ctx, "http://maps.google.com/maps/api/directions/json");
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		String query = "?origin="+params[0]+"&destination="+params[1]+"&sensor=false&mode=walking";
		return super.doInBackground(query);
	}
	
	protected static String getDurationText(JSONObject result) throws JSONException {
		JSONObject duration = getDurationObject(result);
		return duration.getString("text");
	}
	
	protected static int getDurationSeconds(JSONObject result) throws JSONException {
		JSONObject duration = getDurationObject(result);
		return duration.getInt("value");
	}
	
	private static JSONObject getDurationObject(JSONObject result) throws JSONException {
		JSONArray routes = result.getJSONArray("routes");
		JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
		return legs.getJSONObject(0).getJSONObject("duration");
	}
}