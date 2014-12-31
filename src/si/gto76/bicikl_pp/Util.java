package si.gto76.bicikl_pp;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

public class Util {

	public static Location getLocation(double lat, double lng) {
		Location location = new Location("util");
		location.setLatitude(lat);
		location.setLongitude(lng);
		return location;
	}
	
	public static String secondsToText(int seconds) {
		int minutes = 1 + (seconds / 60);
		if (minutes > 59) {
			int hours = minutes / 60;
			int remainingMinutes = minutes % 60;
			return hours + " "+Conf.HOURS_ABR+" " + remainingMinutes + " " + Conf.MINUTES_ABR;
		}
		return minutes + " " + Conf.MINUTES_ABR;
	}
	
	public static List<LatLng> decodePoly(String encoded) {
	    List<LatLng> poly = new ArrayList<LatLng>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;
	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;
	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
	        poly.add(position);
	    }
	    return poly;
	}
}
