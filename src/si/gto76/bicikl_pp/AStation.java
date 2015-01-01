package si.gto76.bicikl_pp;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.ol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
		addLabelsToLayout();
		periodicallyCheckAvailability();
		periodicallyCheckLocation();
		setBackgroundImage();
	}

	private void initializeFields() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		s = new Station(bundle);
		durationText = bundle.getString("durationText", "fetching...");
	}

	// /////////// BUILD GUI

	private void addLabelsToLayout() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(s.name);
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		createTextView("Available: " + s.available, layout, 111);
		createTextView("Free: " + s.free, layout, 222);
		createTextView("Distance: " + durationText, layout, DURATION_TEXT_VIEW);
	}

	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
		layout.removeAllViews();
		addLabelsToLayout();
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

	private void setBackgroundImage() {
		ImageLookUp imageFetcher = new ImageLookUp(getApplicationContext()) {

			@Override
			void onSuccessfulFetch(Bitmap image) throws JSONException {
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainLayout);
				Resources res = getResources();
				if (image == null) {
					System.out.println("#####Image is null.");
				}
				if (res == null) {
					System.out.println("#####Resources is null");
				}
				BitmapDrawable ob = new BitmapDrawable(res, image);
				rl.setBackgroundDrawable(ob);
				
			}
		};
		imageFetcher.execute(((Double) s.location.getLatitude()).toString(),
				((Double) s.location.getLongitude()).toString());
	}

	// /////////// PERIODICALLY CHECK BIKE AVAILABILITY

	private void periodicallyCheckAvailability() {
		final StationsLookUp availabilityChecker = new StationsLookUp(getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				s.available = getAvailableBikes(result, s.id);
				s.free = getFreeSpots(result, s.id);
				resetLayout();
			}
		};
		;

		availabilityChecker.runPeriodically(Conf.updateStationMiliseconds);
	}

	// /////////// PERIODICALLY CHECK LOCATION

	private void periodicallyCheckLocation() {
		new LocationUpdater(this) {
			@Override
			void afterLocationChange(Location location) {
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
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				String durationText = getDurationText(result);
				TextView durationLabel = (TextView) findViewById(DURATION_TEXT_VIEW);
				durationLabel.setText("Distance: " + durationText);
			}
		};
		String[] args = DurationLookUp.getVarArgs(s.location, location);
		durationFetcher.execute(args);
	}

	// /////////// MENU

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.station, menu);
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
		} else if (itemId == R.id.stations) {
			intent = new Intent(this, AStations.class);
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
}
