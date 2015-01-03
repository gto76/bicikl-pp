package si.gto76.bicikl_pp.asynctasks;

import si.gto76.bicikl_pp.Conf;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Simplification of Android's LocationManager class.
 */
public abstract class LocationUpdater {
	
	public abstract void afterLocationChange(Location location);
	
	public LocationUpdater(Activity activity) {
		LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				afterLocationChange(location);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Conf.updateLocationMiliseconds, 
												Conf.updateLocationMeters, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Conf.updateLocationMiliseconds, 
												Conf.updateLocationMeters, locationListener);
	}
	
	
}
