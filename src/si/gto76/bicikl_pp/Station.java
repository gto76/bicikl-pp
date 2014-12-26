package si.gto76.bicikl_pp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Station extends Activity {
	
	private Location stationLocation = null;
	private static final int DURATION_TEXT_VIEW = 333;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String id = extras.getString("id");
//		if (id == null) {
//			finish();
//		} else {
//			TextView title = (TextView) findViewById(R.id.title);
//			title.setText(id);
//		}
		
		getJson(id);
		setLocationManager();
	}
	
	public void getJson(String id) {
		final StationsLookUp occupancy = new StationsLookUpStation(getApplicationContext(), id);
		occupancy.execute("");

		
	}
	
	/////////// GET LOCATION

	public void setLocationManager() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (stationLocation == null) {
					return;
				}
				final DurationLookUpStation duration = new DurationLookUpStation(getApplicationContext());
				String origin = stationLocation.getLatitude()+","+stationLocation.getLongitude();
				String destination = location.getLatitude()+","+location.getLongitude();
				duration.execute(origin, destination);
				
				//TextView durationLabel = (TextView) findViewById(DURATION_TEXT_VIEW);
				//durationLabel.setText(location.toString());
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

	}
	
	///////////// GET STATION DATA
	
	private class StationsLookUpStation extends StationsLookUp {
		
		private String id;

		public StationsLookUpStation(Context ctx, String id) {
			super(ctx);
			this.id = id;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Toast.makeText(context, "Prislo je do napake.", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONObject markers = result.getJSONObject("markers");
				JSONObject station = markers.getJSONObject(id);
				String stationName = station.getString("name");
				
				TextView title = (TextView) findViewById(R.id.title);
				title.setText(stationName);
				
				JSONObject stationAvailability = station.getJSONObject("station");
	            String available = stationAvailability.getString("available");
	            String free = stationAvailability.getString("free");
								
				LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
				createTextView("Available: "+available, layout, 111);
				createTextView("Free: "+free, layout, 222);
				createTextView("Duration: fetching...", layout, DURATION_TEXT_VIEW);
				
				double lat = station.getDouble("lat");
				double lng = station.getDouble("lng");
				Location tempLoc = new Location("station");
				tempLoc.setLatitude(lat);
				tempLoc.setLongitude(lng);
				stationLocation = tempLoc;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	//////////// GET DURATION
	
	private class DurationLookUpStation extends DurationLookUp {

		public DurationLookUpStation(Context ctx) {
			super(ctx);
		}
	
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Toast.makeText(context, "Prislo je do napake.", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONArray routes = result.getJSONArray("routes");
				JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
				JSONObject duration = legs.getJSONObject(0).getJSONObject("duration");
				String durationString = duration.getString("text");
				//int seconds = duration.getInt("value");
				
				TextView durationLabel = (TextView) findViewById(DURATION_TEXT_VIEW);
				durationLabel.setText("Duration: "+durationString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	////////////////// MENU
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.station, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.action_settings:
				return true;
			case R.id.map:
				intent = new Intent(this, Map.class);
				startActivity(intent);
				return true;
			case R.id.stations:
				intent = new Intent(this, Stations.class);
				startActivity(intent);
				return true;
			case R.id.paths:
				intent = new Intent(this, Paths.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	///////////// BUTTONS

	@SuppressLint("NewApi") private void createButton(String name, LinearLayout layout) {
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		Button button = new Button(this);
		button.setLayoutParams(lparams);
		button.setText(name);
		layout.addView(button);
	}
	
	@SuppressLint("NewApi") private void createTextView(String name, LinearLayout layout, int id) {
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextView textView = new TextView(this);
		textView.setLayoutParams(lparams);
		textView.setText(name);
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(29);
		textView.setId(id);
		layout.addView(textView);
	}

	

}
