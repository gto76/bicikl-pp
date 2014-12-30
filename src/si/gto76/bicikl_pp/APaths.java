package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class APaths extends Activity {

	private Location origin;
	private Location destination;

	private List<PathButton> buttons = new ArrayList<PathButton>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paths);

		initializeFields();
		getOriginStations();
	}

	private void initializeFields() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		double originLat = bundle.getDouble("originLat");
		double originLng = bundle.getDouble("originLng");
		origin = Util.getLocation(originLat, originLng);
		double destinatonLat = bundle.getDouble("destinatonLat");
		double destinationLng = bundle.getDouble("destinationLng");
		destination = Util.getLocation(destinatonLat, destinationLng);
	}

	private void getOriginStations() {
		final StationsLookUp stationsFetcher = new StationsLookUp(getApplicationContext()) {

			@Override
			void onSuccessfulFetch(JSONObject result) throws JSONException {
				List<Pair<Float, String>> distancesToOrigin = getDistances(result, origin);
				List<Pair<Float, String>> distancesToDestination = getDistances(result, destination);
				String originStationId = distancesToOrigin.get(0).second;
				String destinationStationId = distancesToDestination.get(0).second;
				final Station originStation = getStationObject(result, originStationId);
				final Station destinationStation = getStationObject(result, destinationStationId);
				final PathButton pathButton = new PathButton(context, originStation, destinationStation);
				buttons.add(pathButton);
				resetLayout();

				// TODO put args into DurationLookUp constructor

				// //// 1. get walking time from origin to originStation
				DurationLookUp durationWalk1Fetcher = new DurationLookUp(getApplicationContext()) {

					@Override
					void onSuccessfulFetch(JSONObject result) throws JSONException {
						pathButton.durationWalk1 = getDurationSeconds(result);
						pathButton.updateText();
						if (pathButton.allSet()) {
							resetLayout();
						}
					}
				};
				String[] args = DurationLookUp.getVarArgs(origin, originStation.location);
				durationWalk1Fetcher.execute(args);

				// //// 2. get distance from originStation to DestinationStation
				// and calculate cycling time
				DurationLookUp durationCycleFetcher = new DurationLookUp(getApplicationContext()) {

					@Override
					void onSuccessfulFetch(JSONObject result) throws JSONException {
						int distanceCycle = getDistanceMeters(result);
						int secondsCycle = (int) (distanceCycle / (Conf.cyclingSpeed * 1000.0 / 3600));
						pathButton.durationCycle = secondsCycle;
						pathButton.updateText();
						if (pathButton.allSet()) {
							resetLayout();
						}
					}
				};
				args = DurationLookUp.getVarArgs(getLocation(result, originStationId),
						getLocation(result, destinationStationId));
				durationCycleFetcher.execute(args);

				// //// 3. get walking time from destinationStation to
				// destination
				DurationLookUp durationWalk2Fetcher = new DurationLookUp(getApplicationContext()) {

					@Override
					void onSuccessfulFetch(JSONObject result) throws JSONException {
						pathButton.durationWalk2 = getDurationSeconds(result);
						pathButton.updateText();
						if (pathButton.allSet()) {
							resetLayout();
						}
					}
				};
				args = DurationLookUp.getVarArgs(destinationStation.location, destination);
				durationWalk2Fetcher.execute(args);
			}

			private List<Pair<Float, String>> getDistances(JSONObject result, Location location)
					throws JSONException {
				List<Pair<Float, String>> distances = new ArrayList<Pair<Float, String>>();
				for (String id : getIds(result)) {
					Location stationLocation = getLocation(result, id);
					float distance = stationLocation.distanceTo(location);
					distances.add(Pair.create(distance, id));
				}
				sortDistances(distances);
				return distances;
			}

			private void sortDistances(List<Pair<Float, String>> distances) {
				Collections.sort(distances, new Comparator<Pair<Float, String>>() {
					@Override
					public int compare(Pair<Float, String> lhs, Pair<Float, String> rhs) {
						return lhs.first.compareTo(rhs.first);
					}
				});
			}
		};
		stationsFetcher.execute();
	}

	// /////////// BUILD GUI

	private void resetLayout() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.pathsLayout);
		layout.removeAllViews();
		Collections.sort(buttons);
		for (Button b : buttons) {
			layout.addView(b);
		}
	}

	@SuppressLint("NewApi")
	private void createButton(Context context, Station originStation, Station destinationStation) {
		// create button
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final PathButton button = new PathButton(context, originStation, destinationStation);
		button.setLayoutParams(lparams);
		// add listener
		Intent intent = new Intent(this, AMap.class);
		PathButtonListener buttonListener = new PathButtonListener(button, intent);
		button.setOnClickListener(buttonListener);
		// add button to list
		buttons.add(button);
	}

	class PathButtonListener implements View.OnClickListener {
		PathButton button;
		private Intent intent;

		public PathButtonListener(PathButton button, Intent intent) {
			this.button = button;
			this.intent = intent;
		}

		@Override
		public void onClick(View v) {
			// TODO: open map activiti with path
			// Bundle bundle = button.getBundle();
			// intent.putExtras(bundle);
			// startActivity(intent);
		}
	}

	// ///////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.paths, menu);
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
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	// /////// PATH BUTTON CLASS

	/**
	 * Button that contains complete info about path. A path consists of four
	 * checkpoints: current location, origin station, destination station and
	 * destination.
	 */
	private class PathButton extends Button implements Comparable<PathButton> {
		// required
		Station originStation;
		Station destinationStation;
		// optional
		Integer durationWalk1, durationCycle, durationWalk2;

		public PathButton(Context context, Station originStation, Station destinationStation) {
			super(context);
			this.originStation = originStation;
			this.destinationStation = destinationStation;
			updateText();
		}

		public Integer getDurationSeconds() {
			if (!allSet()) {
				return Integer.MAX_VALUE;
			}
			return durationWalk1 + durationCycle + durationWalk2;
		}

		private String getDurationText() {
			if (!allSet()) {
				return "fetchig...";
			}
			int seconds = getDurationSeconds();
			return toText(seconds);
		}

		public boolean allSet() {
			return durationWalk1 != null && durationCycle != null && durationWalk2 != null;
		}

		public void updateText() {
			String text = getDurationText() + " | " + "S -> " + toText(durationWalk1) + " -> "
					+ originStation.name + " " + originStation.available + " -> " + toText(durationCycle)
					+ " -> " + destinationStation.name + " " + destinationStation.free + " -> "
					+ toText(durationWalk2) + " -> F";
			this.setText(text);
		}

		private String toText(Integer seconds) {
			if (seconds == null) {
				return "fetching...";
			}
			return Util.secondsToText(seconds);
		}

		@Override
		public int compareTo(PathButton other) {
			return getDurationSeconds().compareTo(other.getDurationSeconds());
		}
	}
}
