package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.asynctasks.ClosestStationsLookUp;
import si.gto76.bicikl_pp.asynctasks.DurationLookUp;
import si.gto76.bicikl_pp.asynctasks.StationsLookUp;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

////////////////////
//// GOOGLE MAP ////
////////////////////

public class AMap extends FragmentActivity {

	private GoogleMap map;
	private Marker destinationMarker;
	private Map<Marker, String> stationIds = new HashMap<Marker, String>();
	private List<Polyline> polylines = new ArrayList<Polyline>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		setMap();
		zoomToStationIfSent();
		drawPolylineIfSent();
		setMarkers();
		setDestinationMarkerListeners();
	}

	// ////////////////////////////////
	// ////////// SET MAP /////////////
	// ////////////////////////////////

	private void setMap() {
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		LatLng defaultLatLng = new LatLng(Conf.DEFAULT_LAT, Conf.DEFAULT_LNG);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, Conf.DEFAULT_ZOOM));
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.setMyLocationEnabled(true);

	}

	private void zoomToStationIfSent() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle == null || !bundle.containsKey("id")) {
			return;
		}
		Station station = new Station(bundle);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(station.getLatLng(), Conf.STATION_ZOOM));
	}

	private void drawPolylineIfSent() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle == null || !bundle.containsKey("polylines")) {
			return;
		}
		String[] polylinesAsStrings = bundle.getStringArray("polylines");
		int color = bundle.getInt("color");
		LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
		for (String polyline : polylinesAsStrings) {
			if (polyline == null) {
				continue;
			}
			PolylineOptions polyOptions = new PolylineOptions().addAll(Util.decodePoly(polyline))
					.geodesic(true).width(Conf.POLYLINE_WIDTH).color(color);

			Polyline line = map.addPolyline(polyOptions);
			polylines.add(line);
			for (LatLng point : line.getPoints()) {
				boundsBuilder = boundsBuilder.include(point);
			}
		}
		LatLngBounds bounds = boundsBuilder.build();
		zoomToBounds(bounds);
	}

	private void zoomToBounds(final LatLngBounds bounds) {
		map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, Conf.PATH_ZOOM_PADDING));
			}
		});
	}

	// /////////////////////////////////
	// ////////// SET MARKERS //////////
	// /////////////////////////////////

	private void setMarkers() {
		final StationsLookUp stationsFetcher = new StationsLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(final JSONObject result) throws JSONException {
				for (final String id : getIds(result)) {
					String name = getName(result, id);
					LatLng latLng = getLatLng(result, id);
					int available = getAvailableBikes(result, id);
					int free = getFreeSpots(result, id);
					String text = available + "/" + free + " " + name;
					float markerColor = getMarkerColor(available, free);
					Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(text)
							.icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
					stationIds.put(marker, id);
				}
				setOnInfoClickListener(result);
			}
		};
		;
		stationsFetcher.execute();
	}

	private float getMarkerColor(int available, int free) {
		if (available == 0) {
			return BitmapDescriptorFactory.HUE_RED; // Empty
		} else if (available < Conf.acceptableAvailability) {
			return BitmapDescriptorFactory.HUE_YELLOW; // Almost empty
		} else if (free == 0) {
			return BitmapDescriptorFactory.HUE_BLUE; // Full
		} else if (free < Conf.acceptableAvailability) {
			return BitmapDescriptorFactory.HUE_AZURE; // Almost full
		} else {
			return BitmapDescriptorFactory.HUE_GREEN; // OK
		}
	}

	/**
	 * sets listener that listens for clicks on the marker info window
	 */
	private void setOnInfoClickListener(final JSONObject result) {
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				try {
					Intent intent = new Intent(AMap.this, AStation.class);
					String id = stationIds.get(marker);
					Bundle bundle = StationsLookUp.getBundle(result, id);
					intent.putExtras(bundle);
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ////////////////////////////////////////////////
	// ///////// DESTINATION MARKER LISTENERS /////////
	// ////////////////////////////////////////////////

	private void setDestinationMarkerListeners() {
		// CREATE:
		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng latLng) {
				if (destinationMarker != null) {
					destinationMarker.remove();
				}
				destinationMarker = map.addMarker(new MarkerOptions().position(latLng).draggable(true)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag_64)));
				clearPolylines();
				drawOptimalPath();
			}

		});
		// DELETE:
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.equals(destinationMarker)) {
					destinationMarker.remove();
					clearPolylines();
					return true;
				}
				return false;
			}
		});
	}

	private void clearPolylines() {
		for (Polyline line : polylines) {
			line.remove();
		}
		polylines.clear();
	}

	// /////////////////////////////
	// ///////// DRAW PATH /////////
	// /////////////////////////////

	private void drawOptimalPath() {
		Location origin = map.getMyLocation();
		Location destination = Util.getLocation(destinationMarker.getPosition());
		ClosestStationsLookUp stationsFetcher = new ClosestStationsLookUp(getApplicationContext(), origin,
				destination, 3, 1, false) {

			@Override
			public void onSuccessfulFetch(List<Pair<Station, Station>> stationPairs) {
				Pair<Station, Station> stations = stationPairs.get(0);
				drawEtapes(origin, stations.first, stations.second, destination);
			}
		};
		stationsFetcher.execute();
	}

	private void drawEtapes(Location origin, Station first, Station second, Location destination) {
		int color = Util.getPathColor(first, second);
		drawEtape(origin, first.location, color);
		drawEtape(first.location, second.location, color);
		drawEtape(second.location, destination, color);
	}

	private void drawEtape(Location origin, Location destination, final int color) {
		DurationLookUp polylineFetcher = new DurationLookUp(getApplicationContext()) {

			@Override
			public void onSuccessfulFetch(JSONObject result) throws JSONException {
				String polyline = getPolyline(result);
				PolylineOptions polyOptions = new PolylineOptions().addAll(Util.decodePoly(polyline))
						.geodesic(true).width(Conf.POLYLINE_WIDTH).color(color);
				Polyline line = map.addPolyline(polyOptions);
				polylines.add(line);
			}
		};
		String[] args = DurationLookUp.getVarArgs(origin, destination);
		polylineFetcher.execute(args);
	}

	// ///////////////////////
	// //////// MENU /////////
	// ///////////////////////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
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
		} else if (itemId == R.id.stations) {
			intent = new Intent(this, AStations.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.paths) {
			Location origin = map.getMyLocation();
			if (destinationMarker == null || origin == null) {
				return false;
			}
			intent = new Intent(this, APaths.class);
			Bundle bundle = new Bundle();
			bundle.putDouble("originLat", origin.getLatitude());
			bundle.putDouble("originLng", origin.getLongitude());
			bundle.putDouble("destinatonLat", destinationMarker.getPosition().latitude);
			bundle.putDouble("destinationLng", destinationMarker.getPosition().longitude);
			intent.putExtras(bundle);
			startActivity(intent);

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
