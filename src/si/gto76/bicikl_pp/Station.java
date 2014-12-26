package si.gto76.bicikl_pp;

import java.util.Timer;
import java.util.TimerTask;

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
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

	/////////////////////////
	//// STATION DETAILS ////
	/////////////////////////

public class Station extends Activity {

	private static final int DURATION_TEXT_VIEW = 333;
	
	private String id;
	private String name;
	private Location stationLocation;
	private int available; 
	private int free;
	private String durationText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station);
		
		initializeFields();
		addLabelsToLayout();
		periodicalyCheckStationData();
		startLocationManager();
	}
	
	private void initializeFields() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		id = bundle.getString("id");
		name = bundle.getString("name");
		double longitude = bundle.getDouble("long");
		double latitude = bundle.getDouble("lat");
		stationLocation = new Location("station");
		stationLocation.setLongitude(longitude);
		stationLocation.setLatitude(latitude);
		available = bundle.getInt("available");
		free = bundle.getInt("free");
		durationText = bundle.getString("durationText");
	}
	
//	private void refreshStationData() {
//		final GetStation station = new GetStation(getApplicationContext(), id);
//		station.execute();
//	}
	
	private void addLabelsToLayout() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(name);
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		createTextView("Available: "+available, layout, 111);
		createTextView("Free: "+free, layout, 222);
		createTextView("Distance: "+durationText, layout, DURATION_TEXT_VIEW);
	}
	
	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		layout.removeAllViews();
		addLabelsToLayout();
	}
	
	///////////// PERIODICALY CHECK BIKE AVAILABILITY

	public void periodicalyCheckStationData() {
		final StationFetcher stationFetcher = new StationFetcher(getApplicationContext(), id);
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							stationFetcher.execute();
						} catch (Exception e) {
						}
					}
				});
			}
		};
		timer.schedule(doAsynchronousTask, 0, Conf.updateStationMiliseconds);
	}

	///////////// STATION FETCHER
	
	private class StationFetcher extends StationsLookUp {
		
		private final String id;

		public StationFetcher(Context context, String id) {
			super(context);
			this.id = id;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Toast.makeText(context, "Error occured while downloading stations data.", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
	            available = getAvailableBikes(result, id);
	            free = getFreeSpots(result, id);
	            resetLayout();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
		
	/////////// START LOCATION MANAGER

	public void startLocationManager() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (stationLocation == null) {
					return;
				}
				final GetDuration duration = new GetDuration(getApplicationContext());
				String origin = stationLocation.getLatitude()+","+stationLocation.getLongitude();
				String destination = location.getLatitude()+","+location.getLongitude();
				duration.execute(origin, destination);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Conf.updateLocationMiliseconds, 
												Conf.updateLocationMeters, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Conf.updateLocationMiliseconds, 
												Conf.updateLocationMeters, locationListener);
	}
	
	//////////// GET DURATION
	
	private class GetDuration extends DurationLookUp {

		public GetDuration(Context ctx) {
			super(ctx);
		}
	
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Toast.makeText(context, "Error occured while downloading directions data.", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				String durationText = getDurationText(result);
				TextView durationLabel = (TextView) findViewById(DURATION_TEXT_VIEW);
				durationLabel.setText("Distance: "+durationText);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	///////////// MAKE BUTTONS

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

	///////////// MAKE  MENU
	
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
}
