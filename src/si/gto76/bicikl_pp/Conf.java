package si.gto76.bicikl_pp;

public class Conf {
	public static final String KEY = "&key=AIzaSyCTBXziQ9NtE633QxhmSqEhRTgfgGldrrk";
	
	public static final double DEFAULT_LAT = 46.0552778;
	public static final double DEFAULT_LNG = 14.5144444;
	public static final int DEFAULT_ZOOM = 13;
	public static final float STATION_ZOOM = 16;
	
	public static final int INITIAL_DELAY_FOR_PERIODIC_TASKS = 3 * 1000;
	public static final int UPDATE_STATION_MILISECONDS = 15 * 1000;
	public static final int UPDATE_LOCATION_MILISECONDS = 30 * 1000;
	public static final int UPDATE_LOCATION_METERS = 50;
	public static final int CYCLING_SPEED = 12;
	public static final int DEFAULT_ACCEPTABLE_AVAILABILITY = 4;
	public static final int NUMBER_OF_GREENS = 2; // Number of stations with acceptable availability considered
	// for inclusion into shortest paths. (including ones with worst availability)
	
	public static final String HOURS_ABR = "h";
	public static final String MINUTES_ABR = "mins";

	public  static final int PATH_ZOOM_PADDING = 50;

	////////////////
	
	public static int updateStationMiliseconds = UPDATE_STATION_MILISECONDS;
	public static int updateLocationMiliseconds = UPDATE_LOCATION_MILISECONDS;
	public static int updateLocationMeters = UPDATE_LOCATION_METERS;
	public static int cyclingSpeed = CYCLING_SPEED;

	public static int acceptableAvailability = DEFAULT_ACCEPTABLE_AVAILABILITY;
}
