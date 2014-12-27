package si.gto76.bicikl_pp;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.Stations.StationButton;

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
		periodicallyCheckAvailability();
		periodicallyCheckLocation();
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

	///////////// BUILD GUI
	
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
	
	///////////// PERIODICALLY CHECK BIKE AVAILABILITY
	
	public void periodicallyCheckAvailability() {
		final StationsLookUp availabilityChecker = new StationsLookUp(
				getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
	            available = getAvailableBikes(result, id);
	            free = getFreeSpots(result, id);
	            resetLayout();
			}
		};
		;

		availabilityChecker.runPeriodically(Conf.updateStationMiliseconds);
	}
	
	///////////// PERIODICALLY CHECK LOCATION

	public void periodicallyCheckLocation() {
		new LocationUpdater(this) {
			@Override
			void afterLocationChange(Location location) {
				if (stationLocation == null) {
					return;
				}
				fetchDuration(location);
			}
		};
	}
	
	private void fetchDuration(final Location location) {
		DurationLookUp durationFetcher = new DurationLookUp(
				getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				String durationText = getDurationText(result);
				TextView durationLabel = (TextView) findViewById(DURATION_TEXT_VIEW);
				durationLabel.setText("Distance: "+durationText);
			}
		};
		
		String origin = stationLocation.getLatitude()+","+stationLocation.getLongitude();
		String destination = location.getLatitude()+","+location.getLongitude();
		durationFetcher.execute(origin, destination);
	}

	///////////// MENU
	
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
