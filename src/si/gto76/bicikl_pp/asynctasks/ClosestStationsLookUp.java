package si.gto76.bicikl_pp.asynctasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.Conf;
import si.gto76.bicikl_pp.Station;

import android.content.Context;
import android.location.Location;
import android.support.v4.util.Pair;

/**
 * Extension of StationsLookUp AsyncTask, used for getting the pairs of stations that form the most promissing
 * paths, according to passed origin and destination locations (by constructor). Its execute method doesen't
 * need any parameters. You have to define onSuccessfulFetch method, that gets the pairs of stations.
 */
public abstract class ClosestStationsLookUp extends StationsLookUp {
	protected final Location origin;
	protected final Location destination;
	protected final int noOfGreens;
	protected final int acceptableAvailability;
	protected final boolean alsoReturnPathsWithouthAvailability;

	public ClosestStationsLookUp(Context context, Location origin, Location destination, int noOfGreens,
			int acceptableAvailability, boolean alsoReturnPathsWithouthAvailability) {
		super(context);
		this.origin = origin;
		this.destination = destination;
		this.noOfGreens = noOfGreens;
		this.acceptableAvailability = acceptableAvailability;
		this.alsoReturnPathsWithouthAvailability = alsoReturnPathsWithouthAvailability;
	}

	public abstract void onSuccessfulFetch(List<Pair<Station, Station>> stationPairs);



	@Override
	public void onSuccessfulFetch(JSONObject result) throws JSONException {
		List<Pair<Float, Station>> originStations = getAirDistances(result, origin);
		List<Pair<Float, Station>> destinationStations = getAirDistances(result, destination);

		// 1. look for closest stations from origin until numberOfGreens with
		// acceptable number of available spots are found
		List<Station> candidateOriginStations = getCandidateStations(originStations, new AvailableOrFree() {

			@Override
			public int getValue(Station station) {
				return station.available;
			}
		});

		// 2. look for closest stations from destination until numberOfGreens with
		// acceptable number of free spots are found
		List<Station> candidateDestinationStations = getCandidateStations(destinationStations,
				new AvailableOrFree() {

					@Override
					public int getValue(Station station) {
						return station.free;
					}
				});

		List<Pair<Station, Station>> stationPairs = getStationPairs(candidateOriginStations,
				candidateDestinationStations);
		
		orderPathsByDurationEstimate(stationPairs);

		onSuccessfulFetch(stationPairs);
	}

	interface AvailableOrFree {
		int getValue(Station station);
	}

	private static List<Pair<Float, Station>> getAirDistances(JSONObject result, Location location)
			throws JSONException {
		List<Pair<Float, Station>> distances = new ArrayList<Pair<Float, Station>>();
		for (String id : getIds(result)) {
			Station station = getStation(result, id);
			float distance = station.location.distanceTo(location);
			distances.add(Pair.create(distance, station));
		}
		sortDistances(distances);
		return distances;
	}

	private static void sortDistances(List<Pair<Float, Station>> distances) {
		Collections.sort(distances, new Comparator<Pair<Float, Station>>() {
			@Override
			public int compare(Pair<Float, Station> lhs, Pair<Float, Station> rhs) {
				return lhs.first.compareTo(rhs.first);
			}
		});
	}

	private List<Station> getCandidateStations(List<Pair<Float, Station>> stationsByDistance,
			AvailableOrFree f) {
		List<Station> candidateStations = new ArrayList<Station>();
		int remainingGreens = noOfGreens;
		for (Pair<Float, Station> stationData : stationsByDistance) {
			Station station = stationData.second;
			if (f.getValue(station) >= acceptableAvailability) {
				remainingGreens--;
			}
			if (f.getValue(station) != 0 || alsoReturnPathsWithouthAvailability) {
				candidateStations.add(station);
			}
			if (remainingGreens == 0) {
				break;
			}
		}
		return candidateStations;
	}

	private static List<Pair<Station, Station>> getStationPairs(List<Station> originStations,
			List<Station> destinationStations) {
		List<Pair<Station, Station>> pairs = new ArrayList<Pair<Station, Station>>();
		for (Station originStation : originStations) {
			for (Station destinationStation : destinationStations) {
				if (!originStation.equals(destinationStation)) {
					pairs.add(new Pair<Station, Station>(originStation, destinationStation));
				}
			}
		}
		return pairs;
	}
	
	
	private void orderPathsByDurationEstimate(List<Pair<Station, Station>> stationPairs) {
		List<Pair<Float, Pair<Station, Station>>> pathsByAirDistance = new ArrayList<Pair<Float, Pair<Station, Station>>>();
		for (Pair<Station, Station> path: stationPairs) {
			float durationEstimate = getDurationEstimate(path);
			pathsByAirDistance.add(new Pair<Float, Pair<Station, Station>>(durationEstimate, path));
		}
		Collections.sort(pathsByAirDistance, new Comparator<Pair<Float, Pair<Station, Station>>>() {

			@Override
			public int compare(Pair<Float, Pair<Station, Station>> lhs,
					Pair<Float, Pair<Station, Station>> rhs) {
				return lhs.first.compareTo(rhs.first);
			}
		});
		stationPairs.clear();
		for (Pair<Float, Pair<Station, Station>> pathData :pathsByAirDistance) {
			stationPairs.add(pathData.second);
		}
	}

	private float getDurationEstimate(Pair<Station, Station> path) {
		float walkingDistance1 = origin.distanceTo(path.first.location);
		float cyclingDistance = path.first.location.distanceTo(path.second.location);
		float walkingDistance2 = path.second.location.distanceTo(destination);
		float cyclingFactor = (float) (5.0 / Conf.cyclingSpeed);
		return walkingDistance1 + cyclingDistance * cyclingFactor + walkingDistance2;
	}

}
