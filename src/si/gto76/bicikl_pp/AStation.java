package si.gto76.bicikl_pp;

import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.DbContract.DbStations;
import si.gto76.bicikl_pp.asynctasks.DurationLookUp;
import si.gto76.bicikl_pp.asynctasks.ImageLookUp;
import si.gto76.bicikl_pp.asynctasks.LocationUpdater;
import si.gto76.bicikl_pp.asynctasks.StationsLookUp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/////////////////////////
//// STATION DETAILS ////
/////////////////////////

public class AStation extends Activity {

	private static final int DURATION_TEXT_VIEW = 333;

	private Station s;
	private String durationText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station);

		initializeFields();
		createLayout();
		setBackgroundImage();
		periodicallyCheckAvailability();
		periodicallyCheckLocation();
	}

	private void initializeFields() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		s = new Station(bundle);
		durationText = bundle.getString("durationText", "fetching...");
	}

	// //////////////////////////////////////////
	// /////////// CREATE LAYOUT ////////////////
	// //////////////////////////////////////////

	private void createLayout() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(s.name);
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		createTextView("Available: " + s.available, layout, 111);
		createTextView("Free: " + s.free, layout, 222);
		createTextView("Distance: " + durationText, layout, DURATION_TEXT_VIEW);
		createButton(layout);
		addHistoricalData();
	}

	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		layout.removeAllViews();
		createLayout();
	}

	@SuppressLint("NewApi")
	private void createTextView(String name, LinearLayout layout, int id) {
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

	private void createButton(LinearLayout layout) {
		// create button
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		Button button = new Button(getApplicationContext());
		button.setLayoutParams(lparams);
		// add listener
		StationButtonListener buttonListener = new StationButtonListener();
		button.setOnClickListener(buttonListener);
		button.setText("Show on Map");
		// add button to layout
		layout.addView(button);
	}

	class StationButtonListener implements View.OnClickListener {
		private Intent intent = new Intent(AStation.this, AMap.class);

		@Override
		public void onClick(View v) {
			Bundle bundle = s.getBundle();
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	// ////////////////////////////////////////////////////
	// /////// SHOW AVAILABILITY HISTORY - DB /////////////
	// ////////////////////////////////////////////////////

	private void addHistoricalData() {
		DbHelper aDbHelper = new DbHelper(getApplicationContext());
		SQLiteDatabase db = aDbHelper.getReadableDatabase();

		String[] select = { DbStations.COLUMN_NAME_TIME, DbStations.COLUMN_NAME_AVAILABLE, DbStations.COLUMN_NAME_FREE };
		String where = DbStations.COLUMN_NAME_STATION_ID + "=?";
		String[] whereWildcards = { s.id };
		String sortOrder = DbStations.COLUMN_NAME_TIME + " DESC";

		Cursor cursor = db.query(DbStations.TABLE_NAME, select, where, whereWildcards, null, null, sortOrder);

		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		createTextView("History:", layout, 444);
		while (cursor.moveToNext()) {
			long time = cursor.getLong(cursor.getColumnIndexOrThrow(DbStations.COLUMN_NAME_TIME));
			int available = cursor.getInt(cursor.getColumnIndexOrThrow(DbStations.COLUMN_NAME_AVAILABLE));
			int free = cursor.getInt(cursor.getColumnIndexOrThrow(DbStations.COLUMN_NAME_FREE));
			displayRow(layout, time, available, free);
		}

		db.close();
	}

	private void displayRow(LinearLayout layout, long time, int available, int free) {
		String date = Util.getDate(time, "dd/MM/yyyy HH:mm");
		createTextView(date + " " + available + "/" + free, layout, 555);
	}

	// /////////////////////////////////////////////
	// ////////////// SET BACKGROUND ///////////////
	// /////////////////////////////////////////////

	private void setBackgroundImage() {
		ImageLookUp imageFetcher = new ImageLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(Bitmap image) throws JSONException {
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainLayout);
				Resources res = getResources();
				BitmapDrawable ob = new BitmapDrawable(res, image);
				rl.setBackgroundDrawable(ob);
			}
		};
		imageFetcher.execute(((Double) s.location.getLatitude()).toString(),
				((Double) s.location.getLongitude()).toString());
	}

	// ////////////////////////////////////////////////////////////
	// /////////// PERIODICALLY CHECK BIKE AVAILABILITY ///////////
	// ////////////////////////////////////////////////////////////

	private void periodicallyCheckAvailability() {
		final StationsLookUp availabilityChecker = new StationsLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(JSONObject result) throws JSONException {
				s.available = getAvailableBikes(result, s.id);
				s.free = getFreeSpots(result, s.id);
				resetLayout();
			}
		};
		;

		availabilityChecker.runPeriodically(Conf.updateStationMiliseconds);
	}

	// ////////////////////////////////////////////////////////
	// /////////// PERIODICALLY CHECK LOCATION ////////////////
	// ////////////////////////////////////////////////////////

	private void periodicallyCheckLocation() {
		new LocationUpdater(this) {
			@Override
			public void afterLocationChange(Location location) {
				if (s.location == null) {
					return;
				}
				fetchDuration(location);
			}
		};
	}

	private void fetchDuration(final Location location) {
		DurationLookUp durationFetcher = new DurationLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(JSONObject result) throws JSONException {
				String durationText = getDurationText(result);
				TextView durationLabel = (TextView) findViewById(DURATION_TEXT_VIEW);
				durationLabel.setText("Distance: " + durationText);
			}
		};
		String[] args = DurationLookUp.getVarArgs(s.location, location);
		durationFetcher.execute(args);
	}

	// ////////////////////////////
	// /////////// MENU ///////////
	// ////////////////////////////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.station, menu);
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
		} else if (itemId == R.id.stations) {
			intent = new Intent(this, AStations.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
