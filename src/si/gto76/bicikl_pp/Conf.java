package si.gto76.bicikl_pp;

public class Conf {
	public static final String KEY = "&key=AIzaSyCTBXziQ9NtE633QxhmSqEhRTgfgGldrrk";
	
	public static final int DEFAULT_CYCLING_SPEED = 12;
	// Number of available bikes and free spots for station marker to be of green color:
	public static final int ACCEPTABLE_AVAILABILITY = 4;
	
	// MAP
	public static final double DEFAULT_LAT = 46.0552778;
	public static final double DEFAULT_LNG = 14.5144444;
	public static final int DEFAULT_ZOOM = 13;
	public static final float STATION_ZOOM = 16;
	public  static final int PATH_ZOOM_PADDING = 50;
	public static final float POLYLINE_WIDTH = 6;
	
	// UPDATES
	public static final int INITIAL_DELAY_FOR_PERIODIC_TASKS = 3 * 1000;
	public static final int UPDATE_STATION_MILISECONDS = 15 * 1000;
	public static final int UPDATE_LOCATION_MILISECONDS = 30 * 1000;
	public static final int UPDATE_LOCATION_METERS = 50;
	
	// PATHS
	// Number of stations with acceptable availability considered
	// for inclusion into shortest paths. (including ones with worst availability):
	public static final int NUMBER_OF_GREENS = 2;
	
	// TIME ABR
	public static final String HOURS_ABR = "h";
	public static final String MINUTES_ABR = "mins";

	// OPTIONS
	public static final int MAX_SPEED = 40;
	public static final int MIN_SPEED = 5;
	public static final int MAX_AVAILABILITY = 12;
	public static final int MIN_AVAILABILITY = 1;

	////////////////

	public static int cyclingSpeed = DEFAULT_CYCLING_SPEED;
	public static int acceptableAvailability = ACCEPTABLE_AVAILABILITY;
	
	public static int updateStationMiliseconds = UPDATE_STATION_MILISECONDS;
	public static int updateLocationMiliseconds = UPDATE_LOCATION_MILISECONDS;
	public static int updateLocationMeters = UPDATE_LOCATION_METERS;
	
}
