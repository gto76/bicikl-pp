package si.gto76.bicikl_pp;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.os.Bundle;

public class Station {
	public final String id;
	public final String name;
	public final Location location;
	public int available;
	public int free;

	public Station(String id, String name, Location location, int available, int free) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.available = available;
		this.free = free;
	}
	
	public Station(String id, String name, double lat, double lng, int available, int free) {
		this.id = id;
		this.name = name;
		this.location = Util.getLocation(lat, lng);
		this.available = available;
		this.free = free;
	}
	
	public Station(Bundle bundle) {
		id = bundle.getString("id");
		name = bundle.getString("name");
		double lat = bundle.getDouble("lat");
		double lng = bundle.getDouble("lng");
		location = Util.getLocation(lat, lng);
		available = bundle.getInt("available");
		free = bundle.getInt("free");
	}
	
	///////
	
	public Bundle getBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		bundle.putDouble("lat", location.getLatitude());
		bundle.putDouble("lng", location.getLongitude());
		bundle.putInt("available", available);
		bundle.putInt("free", free);
		return bundle;
	}

	public LatLng getLatLng() {
		return new LatLng(location.getLatitude(), location.getLongitude());
	}
	
	@Override
	public boolean equals(Object o) {
		Station other = (Station) o;
		return id.equals(other.id);
	}
}