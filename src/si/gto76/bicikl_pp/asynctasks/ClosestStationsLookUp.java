package si.gto76.bicikl_pp.asynctasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.or;

import si.gto76.bicikl_pp.APaths;
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

	interface AvailableOrFree {
		int getValue(Station station);
	}

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

		onSuccessfulFetch(stationPairs);
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
				if (originStation != destinationStation) {
					pairs.add(new Pair<Station, Station>(originStation, destinationStation));
				}
			}
		}
		return pairs;
	}

}
