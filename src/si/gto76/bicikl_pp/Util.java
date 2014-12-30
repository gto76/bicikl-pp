package si.gto76.bicikl_pp;

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
}
