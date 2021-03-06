package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.asynctasks.DurationLookUp;
import si.gto76.bicikl_pp.asynctasks.ImageLookUp;
import si.gto76.bicikl_pp.asynctasks.LocationUpdater;
import si.gto76.bicikl_pp.asynctasks.StationsLookUp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

//////////////////////////
//// LIST OF STATIONS ////
//////////////////////////

public class AStations extends Activity {

	private List<StationButton> buttons = new ArrayList<StationButton>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stations);

		getStationsDataAndBuildGui();
		periodicallyCheckAvailability();
		periodicallyCheckLocation();
		setBackgroundImage();
	}

	// //////////////////////////////////////////
	// /////////// GET STATIONS DATA ////////////
	// //////////////////////////////////////////

	private void getStationsDataAndBuildGui() {
		final StationsLookUp stationsFetcher = new StationsLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(JSONObject result) throws JSONException {
				for (Station station : getStations(result)) {
					createButton(station);
				}
			}
		};
		stationsFetcher.execute();
	}

	// ////////////////////////////////////
	// /////////// BUILD GUI //////////////
	// ////////////////////////////////////

	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		layout.removeAllViews();
		addButtonsToLayout();
	}

	private void addButtonsToLayout() {
		Collections.sort(buttons);
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		for (Button b : buttons) {
			layout.addView(b);
		}
	}

	@SuppressLint("NewApi")
	private void createButton(Station station) {
		// create button
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		StationButton button = new StationButton(this, station);
		button.setLayoutParams(lparams);
		button.setTextColor(Color.WHITE);
		// add button to list
		buttons.add(button);
		resetLayout();
	}

	private void setBackgroundImage() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location == null) {
			return;
		}

		ImageLookUp imageFetcher = new ImageLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(Bitmap image) throws JSONException {
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainLayout);
				Resources res = getResources();
				BitmapDrawable ob = new BitmapDrawable(res, image);
				rl.setBackgroundDrawable(ob);
			}
		};
		imageFetcher.execute(((Double) location.getLatitude()).toString(),
				((Double) location.getLongitude()).toString());
	}

	// ////////////////////////////////////////////////////////////////
	// /////////// PERIODICALLY CHECK BIKE AVAILABILITY ///////////////
	// ////////////////////////////////////////////////////////////////

	private void periodicallyCheckAvailability() {
		final StationsLookUp availabilityChecker = new StationsLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(JSONObject result) throws JSONException {
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

	// //////////////////////////////////////////////////////
	// /////////// PERIODICALLY CHECK LOCATION //////////////
	// //////////////////////////////////////////////////////

	private void periodicallyCheckLocation() {
		new LocationUpdater(this) {
			@Override
			public void afterLocationChange(Location location) {
				for (StationButton button : buttons) {
					fetchDuration(location, button);
				}
			}
		};
	}

	private void fetchDuration(final Location location, final StationButton button) {
		DurationLookUp durationFetcher = new DurationLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(JSONObject result) throws JSONException {
				button.durationText = getDurationText(result);
				button.durationSeconds = getDurationSeconds(result);
				button.updateText();
				resetLayout();
			}
		};
		String[] args = DurationLookUp.getVarArgs(button.s.location, location);
		durationFetcher.execute(args);
	}

	// /////////////////////////////
	// /////////// MENU ////////////
	// /////////////////////////////

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
			intent = new Intent(this, AOptions.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.map) {
			intent = new Intent(this, AMap.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	// ////////////////////////////////////////////////
	// /////////// STATION BUTTON CLASS ///////////////
	// ////////////////////////////////////////////////

	private class StationButton extends Button implements Comparable<StationButton> {
		// required
		final Station s;
		// optional
		Integer durationSeconds = Integer.MAX_VALUE;
		String durationText = "fetching... ";

		public StationButton(Context context, Station s) {
			super(context);
			this.s = s;
			addListener();
			updateText();
		}

		private void addListener() {
			StationButtonListener buttonListener = new StationButtonListener();
			this.setOnClickListener(buttonListener);
		}

		public void updateText() {
			String text = durationText + " " + s.name + " " + s.available + "/" + s.free;
			this.setText(text);
		}

		@Override
		public int compareTo(StationButton other) {
			return this.durationSeconds.compareTo(other.durationSeconds);
		}

		class StationButtonListener implements View.OnClickListener {
			private Intent intent = new Intent(AStations.this, AStation.class);

			@Override
			public void onClick(View v) {
				Bundle bundle = s.getBundle();
				bundle.putInt("durationSeconds", durationSeconds);
				bundle.putString("durationText", durationText);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}
	}
}
