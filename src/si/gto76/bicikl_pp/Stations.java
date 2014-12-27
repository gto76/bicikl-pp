package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

	//////////////////////////
	//// LIST OF STATIONS ////
	//////////////////////////

public class Stations extends Activity {

	private List<StationButton> buttons = new ArrayList<StationButton>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stations);
		
		getStationsDataAndBuildGui();
		periodicallyCheckAvailability();
		periodicallyCheckLocation();
	}
	
	///////////// GET STATIONS DATA
	
	private void getStationsDataAndBuildGui() {
		final StationsLookUp stationsDataFetcher = new StationsLookUp(getApplicationContext()) {
			
			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				JSONObject markers = result.getJSONObject("markers");
				Iterator<String> iter = markers.keys();
			    while (iter.hasNext()) {
			        String id = iter.next();
			        parseStation(result, id);
			    }
			}

			private void parseStation(JSONObject result, String id) throws JSONException {
				// parse JSON
				String stationName = getStationName(result, id);
				Location location = getLocation(result, id);
				int available = getAvailableBikes(result, id);
				int free = getFreeSpots(result, id);
				// create button
				createButton(id, stationName, location, available, free);
			}
		};
		stationsDataFetcher.execute();
	}
	
	///////////// BUILD GUI
	
	private void addButtonsToLayout() {
		Collections.sort(buttons);
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		for (Button b: buttons) {
			layout.addView(b);
		}
	}
	
	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		layout.removeAllViews();
		addButtonsToLayout();
	}

	@SuppressLint("NewApi") private void createButton(String id, String name, 
														Location location, int available, int free) {
		// create button
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		StationButton button = new StationButton(this, id, name, location, available, free);
		button.setLayoutParams(lparams);
		// add listener
		Intent intent = new Intent(this, Station.class);
		UserButtonListener buttonListener = new UserButtonListener(button, intent);
		button.setOnClickListener(buttonListener);
		// add button to list
		buttons.add(button);
	}
	

	class UserButtonListener implements View.OnClickListener {
		StationButton button;
		private Intent intent;
		public UserButtonListener(StationButton button, Intent intent) { 
			this.button = button;
			this.intent = intent;
		}
		@Override
		public void onClick(View v) {
			Bundle bundle = new Bundle(); 
			button.fillBundle(bundle);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
	
	///////////// PERIODICALLY CHECK BIKE AVAILABILITY
	
	public void periodicallyCheckAvailability() {
		final StationsLookUp availabilityChecker = new StationsLookUp(
				getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				for (StationButton b : buttons) {
					String id = b.id;
					b.available = getAvailableBikes(result, id);
					b.free = getFreeSpots(result, id);
					b.updateText();
				}
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
				for (StationButton button: buttons) {
					fetchDuration(location, button);
				}
			}
		};
	}

	private void fetchDuration(final Location location, final StationButton button) {
		DurationLookUp durationFetcher = new DurationLookUp(
				getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				button.durationText = getDurationText(result);
				button.durationSeconds = getDurationSeconds(result);
				button.updateText();
				resetLayout();
			}
		};
		
		String origin = button.location.getLatitude()+","+button.location.getLongitude();
		String destination = location.getLatitude()+","+location.getLongitude();
		durationFetcher.execute(origin, destination);
	}

	///////////// MENU

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stations, menu);
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
			case R.id.paths:
				intent = new Intent(this, Paths.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	///////////// STATION BUTTON CLASS
	
	class StationButton extends Button implements Comparable<StationButton> {
		// required fields
		final String id;
		final String name;
		final Location location;
		int available;
		int free;
		// optional fields
		Integer durationSeconds = Integer.MAX_VALUE;
		String durationText = "fetching... ";
		
		public StationButton(Context context, String id, String name, Location location, int available, int free) {
			super(context);
			this.id = id;
			this.name = name;
			this.location = location;
			this.available = available;
			this.free = free;
			updateText();
		}

		public void updateText() {
			String text = durationText + " " + name + " " + available + "/" + free;
			this.setText(text);
		}

		public void fillBundle(Bundle bundle) {
			bundle.putString("id", id);
			bundle.putString("name", name);
			bundle.putDouble("long", location.getLongitude());
			bundle.putDouble("lat", location.getLatitude());
			bundle.putInt("available", available);
			bundle.putInt("free", free);
			bundle.putInt("durationSeconds", durationSeconds);
			bundle.putString("durationText", durationText);
		}
		
		@Override
	    public int compareTo(StationButton other){
			return this.durationSeconds.compareTo(other.durationSeconds);
	    }
	}
}
