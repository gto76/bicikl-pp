package si.gto76.bicikl_pp;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AMap extends FragmentActivity {

	private GoogleMap googleMap;
	private Marker destinationMarker;
	private Map<Marker, String> stationIds = new HashMap<Marker, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		setMap();
	}

	private void setMap() {
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.0552778, 14.5144444), 13));
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setMyLocationEnabled(true);
		setMarkers();
		setMapListener();
	}


	private void setMarkers() {
		final StationsLookUp stationsFetcher = new StationsLookUp(getApplicationContext()) {

			@Override
			void onSuccessfulFetch(final JSONObject result) throws JSONException {
				for (final String id : getIds(result)) {
					String name = getName(result, id);
					LatLng latLng = getLatLng(result, id);
					int available = getAvailableBikes(result, id);
					int free = getFreeSpots(result, id);
					String text =  available + "/" + free +" "+ name;
					float markerColor = getMarkerColor(available, free);
					Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(text)
							.icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
					stationIds.put(marker, id);
				}
				setInfoListener(result);
			}
		};
		;
		stationsFetcher.execute();
	}

	private float getMarkerColor(int available, int free) {
		if (available == 0) {
			return BitmapDescriptorFactory.HUE_RED; // Empty
		} else if (available <= 3) {
			return BitmapDescriptorFactory.HUE_YELLOW; // Almost empty
		} else if (free == 0) {
			return BitmapDescriptorFactory.HUE_VIOLET; // Full
		} else if (free <= 3) {
			return BitmapDescriptorFactory.HUE_BLUE; // Almost full
		} else {
			return BitmapDescriptorFactory.HUE_GREEN; // OK
		}
	}
	
	// sets listener that listens for clicks on the info window
	private void setInfoListener(final JSONObject result) {
		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				try {
					Intent intent = new Intent(AMap.this, AStation.class);
					String id =  stationIds.get(marker);
					Bundle bundle = StationsLookUp.getBundle(result, id);
					intent.putExtras(bundle);
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void setMapListener() {
		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng latLng) {
				if (destinationMarker != null) {
					destinationMarker.remove();
				}
	            destinationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true)
	            		.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag_64)));
			}
	    });
	}

	// ////

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
			return true;
		} else if (itemId == R.id.stations) {
			intent = new Intent(this, AStations.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.paths) {
			Location origin = googleMap.getMyLocation();
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
