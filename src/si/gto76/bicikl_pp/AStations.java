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

// TODO add api key

public class AStations extends Activity {

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
		final StationsLookUp stationsFetcher = new StationsLookUp(getApplicationContext()) {
			
			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				for (Station station: getStations(result)) {
					createButton(station);
				}
			}
		};
		stationsFetcher.execute();
	}
	
	///////////// BUILD GUI
	
	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		layout.removeAllViews();
		addButtonsToLayout();
	}
	
	private void addButtonsToLayout() {
		Collections.sort(buttons);
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		for (Button b: buttons) {
			layout.addView(b);
		}
	}

	@SuppressLint("NewApi") private void createButton(Station station) {
		// create button
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		StationButton button = new StationButton(this, station);
		button.setLayoutParams(lparams);
		// add listener
		Intent intent = new Intent(this, AStation.class);
		StationButtonListener buttonListener = new StationButtonListener(button, intent);
		button.setOnClickListener(buttonListener);
		// add button to list
		buttons.add(button);
		resetLayout();
	}

	class StationButtonListener implements View.OnClickListener {
		StationButton button;
		private Intent intent;
		public StationButtonListener(StationButton button, Intent intent) { 
			this.button = button;
			this.intent = intent;
		}
		@Override
		public void onClick(View v) {
			Bundle bundle = button.getBundle();
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
	
	///////////// PERIODICALLY CHECK BIKE AVAILABILITY
	
	private void periodicallyCheckAvailability() {
		final StationsLookUp availabilityChecker = new StationsLookUp(
				getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				for (StationButton b : buttons) {
					String id = b.s.id;
					b.s.available = getAvailableBikes(result, id);
					b.s.free = getFreeSpots(result, id);
					b.updateText();
				}
			}
		};
		;

		availabilityChecker.runPeriodically(Conf.updateStationMiliseconds);
	}
		
	///////////// PERIODICALLY CHECK LOCATION

	private void periodicallyCheckLocation() {
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
		String[] args = DurationLookUp.getVarArgs(button.s.location, location);
		durationFetcher.execute(args);
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
		int itemId = item.getItemId();
		if (itemId == R.id.action_settings) {
			return true;
		} else if (itemId == R.id.map) {
			intent = new Intent(this, AMap.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.paths) {
			intent = new Intent(this, APaths.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	///////////// STATION BUTTON CLASS
	
	private class StationButton extends Button implements Comparable<StationButton> {
		// required
		final Station s;
		// optional
		Integer durationSeconds = Integer.MAX_VALUE;
		String durationText = "fetching... ";
		
		public StationButton(Context context, Bundle bundle) {
			super(context);
			s = new Station(bundle);
			updateText();
		}
		
		public StationButton(Context context, Station s) {
			super(context);
			this.s = s;
			updateText();
		}

		public void updateText() {
			String text = durationText + " " + s.name + " " + s.available + "/" + s.free;
			this.setText(text);
		}

		public Bundle getBundle() {
			Bundle bundle = s.getBundle();
			bundle.putInt("durationSeconds", durationSeconds);
			bundle.putString("durationText", durationText);
			return bundle;
		}
		
		@Override
	    public int compareTo(StationButton other){
			return this.durationSeconds.compareTo(other.durationSeconds);
	    }
	}
}
