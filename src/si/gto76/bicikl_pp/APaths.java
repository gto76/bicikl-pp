package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;

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
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
		setBackgroundImage();
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
				List<Pair<Float, Station>> distancesToOrigin = getDistances(result, origin);
				List<Pair<Float, Station>> distancesToDestination = getDistances(result, destination);
				
				// 1. look for closest stations from origin until two of the green ones are found
				distancesToOrigin = trancuateDistances(distancesToOrigin, new AvailableOrFree() {
					
					@Override
					public int getValue(Station station) {
						return station.available;
					}
				});
				
				// 2. look for closest stations from destination until two of the green ones are found
				distancesToDestination = trancuateDistances(distancesToDestination, new AvailableOrFree() {
					
					@Override
					public int getValue(Station station) {
						return station.free;
					}
				});
				
				// 3. get paths containing all of the above combinations
				createButtonsFromBestPaths(distancesToOrigin, distancesToDestination);
				resetLayout();
				
				// 4. for each button calculate duration
				for (PathButton button: buttons) {
					calculateDuration(button, origin, destination);
				}
			}

			private void calculateDuration(final PathButton button, Location origin, Location destination) {
				// //// 1. get walking time from origin to originStation
				DurationLookUp durationWalk1Fetcher = new DurationLookUp(getApplicationContext()) {

					@Override
					void onSuccessfulFetch(JSONObject result) throws JSONException {
						button.durationWalk1 = getDurationSeconds(result);
						button.polylines[0] = getPolyline(result);
						updateTextAndResetLayout(button);
					}

				};
				String[] args = DurationLookUp.getVarArgs(origin, button.originStation.location);
				durationWalk1Fetcher.execute(args);

				// //// 2. get distance from originStation to DestinationStation and calculate cycling time
				DurationLookUp durationCycleFetcher = new DurationLookUp(getApplicationContext()) {

					@Override
					void onSuccessfulFetch(JSONObject result) throws JSONException {
						int distanceCycle = getDistanceMeters(result);
						int secondsCycle = (int) (distanceCycle / (Conf.cyclingSpeed * 1000.0 / 3600));
						button.durationCycle = secondsCycle;
						button.polylines[1] = getPolyline(result);
						updateTextAndResetLayout(button);
					}
				};
				args = DurationLookUp.getVarArgs(button.originStation.location, button.destinationStation.location);
				durationCycleFetcher.execute(args);

				// //// 3. get walking time from destinationStation to destination
				DurationLookUp durationWalk2Fetcher = new DurationLookUp(getApplicationContext()) {

					@Override
					void onSuccessfulFetch(JSONObject result) throws JSONException {
						button.durationWalk2 = getDurationSeconds(result);
						button.polylines[2] = getPolyline(result);
						updateTextAndResetLayout(button);
					}
				};
				args = DurationLookUp.getVarArgs(button.destinationStation.location, destination);
				durationWalk2Fetcher.execute(args);
			}
			
			private void updateTextAndResetLayout(PathButton button) {
				button.updateText();
				if (button.allSet()) {
					resetLayout();
				}
			}

			private List<Pair<Float, Station>> getDistances(JSONObject result, Location location)
					throws JSONException {
				List<Pair<Float, Station>> distances = new ArrayList<Pair<Float, Station>>();
				for (String id : getIds(result)) {
					Station station = getStationObject(result, id);
					float distance = station.location.distanceTo(location);
					distances.add(Pair.create(distance, station));
				}
				sortDistances(distances);
				return distances;
			}

			private void sortDistances(List<Pair<Float, Station>> distances) {
				Collections.sort(distances, new Comparator<Pair<Float, Station>>() {
					@Override
					public int compare(Pair<Float, Station> lhs, Pair<Float, Station> rhs) {
						return lhs.first.compareTo(rhs.first);
					}
				});
			}
		};
		stationsFetcher.execute();
	}
	
	interface AvailableOrFree {
        int getValue(Station station);
    }

	private List<Pair<Float, Station>> trancuateDistances(List<Pair<Float, Station>> distancesToOrigin, AvailableOrFree f) {
		int remainingGreens = Conf.NUMBER_OF_GREENS;
		int i = 0;
		for (Pair<Float, Station> distanceAndStation : distancesToOrigin) {
			Station station = distanceAndStation.second;
			if (f.getValue(station) >= Conf.acceptableAvailability) {
				remainingGreens--;
			}
			i++;
			if (remainingGreens == 0) {
				break;
			}
		}
		return distancesToOrigin.subList(0, i);
	}
	
	private void createButtonsFromBestPaths(List<Pair<Float, Station>> distancesToOrigin,
			List<Pair<Float, Station>> distancesToDestination) {
		// for every origin station
		for (Pair<Float, Station> distanceAndStation : distancesToOrigin) {
			Station originStation = distanceAndStation.second;
			for (Pair<Float, Station> distanceAndStation2 : distancesToDestination) {
				Station destinationStation = distanceAndStation2.second;
				if (originStation != destinationStation) {
					createButton(getApplicationContext(), originStation, destinationStation);
				}
			}
		}
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
		button.setAlpha((float)0.75);
		// add button to list
		buttons.add(button);
	}


	
	private void setBackgroundImage() {
		ImageLookUp imageFetcher = new ImageLookUp(getApplicationContext()) {

			@Override
			void onSuccessfulFetch(Bitmap image) throws JSONException {
				if (image == null) {
					return;
				}
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainLayout);
				Resources res = getResources();
				BitmapDrawable ob = new BitmapDrawable(res, image);
				rl.setBackgroundDrawable(ob);
			}
		};
		imageFetcher.execute(((Double) destination.getLatitude()).toString(),
				((Double) destination.getLongitude()).toString());
	}

	// ///////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

	// ######
	// ###### PATH BUTTON CLASS
	// ######

	/**
	 * Button that contains complete info about path. A path consists of four
	 * checkpoints: current location, origin station, destination station and
	 * destination.
	 */
	private class PathButton extends Button implements Comparable<PathButton> {
		// required
		Station originStation;
		Station destinationStation;
		int color;
		// optional
		Integer durationWalk1;
		Integer durationCycle;
		Integer durationWalk2;
		String[] polylines = new String[3]; 

		public PathButton(Context context, Station originStation, Station destinationStation) {
			super(context);
			this.originStation = originStation;
			this.destinationStation = destinationStation;
			setColor();
			updateText();
			addListener();
		}
		
		////////////// COLOR

		private void setColor() {
			int weakestLink = Math.min(originStation.available, destinationStation.free);
			if (weakestLink >= Conf.acceptableAvailability) {
				color = Color.GREEN;
			} else if (weakestLink > 0) {
				color = Color.YELLOW;
			} else {
				color = Color.RED;
			}
			this.setBackgroundColor(color);
		}

		////////////// TEXT

		public void updateText() {
			String text = getDurationText() + " | " + toText(durationWalk1) + " -> "
					+ originStation.name + " " + originStation.available + " -> " + toText(durationCycle)
					+ " -> " + destinationStation.name + " " + destinationStation.free + " -> "
					+ toText(durationWalk2);
			this.setText(text);
		}
		
		private String getDurationText() {
			if (!allSet()) {
				return "fetchig...";
			}
			int seconds = getDurationSeconds();
			return toText(seconds);
		}
		
		private Integer getDurationSeconds() {
			if (!allSet()) {
				return Integer.MAX_VALUE;
			}
			return durationWalk1 + durationCycle + durationWalk2;
		}
		
		private boolean allSet() {
			return durationWalk1 != null && durationCycle != null && durationWalk2 != null;
		}

		private String toText(Integer seconds) {
			if (seconds == null) {
				return "fetching...";
			}
			return Util.secondsToText(seconds);
		}
		
		////////// SHOW ON MAP LISTENER

		private void addListener() {
			PathButtonListener buttonListener = new PathButtonListener();
			this.setOnClickListener(buttonListener);
		}
		
		class PathButtonListener implements View.OnClickListener {
			private Intent intent = new Intent(APaths.this, AMap.class);
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putStringArray("polylines", PathButton.this.polylines);
				bundle.putInt("color", color);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}

		@Override
		public int compareTo(PathButton other) {
			return getDurationSeconds().compareTo(other.getDurationSeconds());
		}
	}
}
