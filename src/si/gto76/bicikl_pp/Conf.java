package si.gto76.bicikl_pp;

import java.util.Date;

public class Conf {
	//static final String KEY = "";
	static final String KEY = "&key=AIzaSyCTBXziQ9NtE633QxhmSqEhRTgfgGldrrk";
	
	//static final int STATION_IMAGE_WIDTH = 500;
	//static final int STATION_IMAGE_HEIGHT = 1000;
	
	static final double DEFAULT_LAT = 46.0552778;
	static final double DEFAULT_LNG = 14.5144444;
	static final int DEFAULT_ZOOM = 13;
	public static final float STATION_ZOOM = 16;
	
	static final int INITIAL_DELAY_FOR_PERIODIC_TASKS = 3 * 1000;
	static final int UPDATE_STATION_MILISECONDS = 15 * 1000;
	static final int UPDATE_LOCATION_MILISECONDS = 30 * 1000;
	static final int UPDATE_LOCATION_METERS = 50;
	static final int CYCLING_SPEED = 12;
	static final int DEFAULT_ACCEPTABLE_AVAILABILITY = 4;
	static final int NUMBER_OF_GREENS = 2; // Number of stations with acceptable availability considered
	// for inclusion into shorthest paths. (including ones with worst availability)
	
	static final String HOURS_ABR = "h";
	static final String MINUTES_ABR = "mins";

	protected static final int PATH_ZOOM_PADDING = 50;


	
	static int updateStationMiliseconds = UPDATE_STATION_MILISECONDS;
	static int updateLocationMiliseconds = UPDATE_LOCATION_MILISECONDS;
	static int updateLocationMeters = UPDATE_LOCATION_METERS;
	static int cyclingSpeed = CYCLING_SPEED;

	static int acceptableAvailability = DEFAULT_ACCEPTABLE_AVAILABILITY;
}
