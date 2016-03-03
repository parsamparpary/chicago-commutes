/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.data;

import android.util.Log;
import android.util.SparseArray;
import au.com.bytecode.opencsv.CSVReader;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.entity.factory.StationFactory;
import fr.cph.chicago.entity.factory.StopFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
// TODO see if there is anyway to do it with better performance
public class TrainData {
	/**
	 * Tag
	 **/
	private static final String TAG = TrainData.class.getSimpleName();

	private SparseArray<Station> stations;
	private List<Station> stationsOrderByName;
	private List<Station> stationsOrderByLine;
	private Map<TrainLine, List<Station>> stationsOrderByLineMap;
	private SparseArray<Stop> stops;

	/**
	 * Constructor
	 */
	public TrainData() {
		this.stations = new SparseArray<>();
		this.stops = new SparseArray<>();
	}

	/**
	 * Read train data from CSV file.
	 */
	public final void read() {
		if (stations.size() == 0 && stops.size() == 0) {
			try {
				final CSVReader reader = new CSVReader(new InputStreamReader(ChicagoTracker.getContext().getAssets().open("cta_L_stops_cph.csv")));
				reader.readNext();
				String[] row;
				while ((row = reader.readNext()) != null) {
					final Integer stopId = Integer.valueOf(row[0]); // STOP_ID
					final TrainDirection direction = TrainDirection.fromString(row[1]); // DIRECTION_ID
					final String stopName = row[2]; // STOP_NAME
					final String stationName = row[3];// STATION_NAME
					// String stationDescription = row[4];//STATION_DESCRIPTIVE_NAME
					final Integer parentStopId = Integer.valueOf(row[5]);// MAP_ID (old PARENT_STOP_ID)
					final Boolean ada = Boolean.valueOf(row[6]);// ADA
					final List<TrainLine> lines = new ArrayList<>();
					String red = row[7];// Red
					String blue = row[8];// Blue
					String green = row[9];// G
					String brown = row[10];// Brn
					String purple = row[11];// P
					String purpleExp = row[12];// Pexp
					String yellow = row[13];// Y
					String pink = row[14];// Pink
					String orange = row[15];// Org
					if (red.equals("TRUE")) {
						lines.add(TrainLine.RED);
					}
					if (blue.equals("TRUE")) {
						lines.add(TrainLine.BLUE);
					}
					if (brown.equals("TRUE")) {
						lines.add(TrainLine.BROWN);
					}
					if (green.equals("TRUE")) {
						lines.add(TrainLine.GREEN);
					}
					if (purple.equals("TRUE")) {
						// PURPLE_EXPRESS MOD
						if (!lines.contains(TrainLine.PURPLE)) {
							lines.add(TrainLine.PURPLE);
						}
					}
					if (purpleExp.equals("TRUE")) {
						// PURPLE_EXPRESS MOD
						if (!lines.contains(TrainLine.PURPLE)) {
							lines.add(TrainLine.PURPLE);
						}
					}
					if (yellow.equals("TRUE")) {
						lines.add(TrainLine.YELLOW);
					}
					if (pink.equals("TRUE")) {
						lines.add(TrainLine.PINK);
					}
					if (orange.equals("TRUE")) {
						lines.add(TrainLine.ORANGE);
					}
					final String location = row[16];// Location
					final String locationTrunk = location.substring(1);
					final String coordinates[] = locationTrunk.substring(0, locationTrunk.length() - 1).split(", ");
					final Double longitude = Double.valueOf(coordinates[0]);
					final Double latitude = Double.valueOf(coordinates[1]);

					final Stop stop = StopFactory.buildStop(stopId, stopName, direction);
					stop.setPosition(new Position(longitude, latitude));
					final Station station = StationFactory.buildStation(parentStopId, stationName, null);
					// stop.setStation(station);
					stop.setAda(ada);
					stop.setLines(lines);
					stops.append(stopId, stop);

					final Station currentStation = stations.get(parentStopId, null);
					if (currentStation == null) {
						final List<Stop> st = new ArrayList<>();
						st.add(stop);
						station.setStops(st);
						stations.append(parentStopId, station);
					} else {
						currentStation.getStops().add(stop);
					}
				}
				reader.close();
				order();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	/**
	 * Get all stations
	 *
	 * @return a map containing all the stations ordered line
	 */
	public final Map<TrainLine, List<Station>> getAllStations() {
		return stationsOrderByLineMap;
	}

	/**
	 * Get a list of station for a given line
	 *
	 * @param line the train line
	 * @return a list of station
	 */
	public final List<Station> getStationsForLine(final TrainLine line) {
		return stationsOrderByLineMap.get(line);
	}

	/**
	 * get a station
	 *
	 * @param id the id of the station
	 * @return
	 */
	public final Station getStation(final Integer id) {
		if (stations.size() != 0) {
			return stations.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Get a station with its position in the list
	 *
	 * @param position the position of the station in the list
	 * @return a station
	 */
	public final Station getStationByPosition(final int position) {
		if (stations.size() != 0 && position <= stations.size()) {
			return stations.valueAt(position);
		} else {
			return null;
		}
	}

	/**
	 * Get a station with its position in the ordered by name list
	 *
	 * @param position the position
	 * @return a station
	 */
	public final Station getStationByPositionAndName(final int position) {
		if (stationsOrderByName.size() != 0 && position <= stationsOrderByName.size()) {
			return stationsOrderByName.get(position);
		} else {
			return null;
		}
	}

	/**
	 * Get station by position and line
	 *
	 * @param position the position
	 * @return a station
	 */
	public final Station getStationByPositionAndLine(final int position) {
		if (stationsOrderByLine.size() != 0 && position <= stationsOrderByLine.size()) {
			return stationsOrderByLine.get(position);
		} else {
			return null;
		}
	}

	/**
	 * Get stations size
	 *
	 * @return the size of the stations list
	 */
	public final int getStationsSize() {
		return stations.size();
	}

	public final boolean isStationNull() {
		return stations == null;
	}

	/**
	 * Get station size from the ordered by line
	 *
	 * @return the size
	 */
	public final int getStationsSizeByLine() {
		return stationsOrderByLine.size();
	}

	/**
	 * Get station by name
	 *
	 * @param name the name of the station
	 * @return a station
	 */
	public final Station getStationByName(final String name) {
		int index = 0;
		while (index < stations.size()) {
			final Station station = stations.valueAt(index++);
			if (station.getName().equals(name)) {
				return station;
			}
		}
		return null;
	}

	public final boolean isStopsNull() {
		return stops == null;
	}

	/**
	 * Get a stop
	 *
	 * @param id the id of the stop
	 * @return a stop
	 */
	public final Stop getStop(final Integer id) {
		if (stops.size() != 0) {
			return stops.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Get a stop from the list
	 *
	 * @param position the position of the stop in the list
	 * @return a stop
	 */
	public final Stop getStopByPosition(final int position) {
		if (stops.size() != 0) {
			return stops.valueAt(position);
		} else {
			return null;
		}
	}

	/**
	 * Get the size of the stops found
	 *
	 * @return a size
	 */
	public final int getStopsSize() {
		return stops.size();
	}

	/**
	 * Get stop by desc
	 *
	 * @param desc the desription of stop
	 * @return a stop
	 */
	public final Stop getStopByDesc(final String desc) {
		int index = 0;
		while (index < stops.size()) {
			final Stop stop = stops.valueAt(index++);
			if (stop.getDescription().equals(desc) || stop.getDescription().split(" ")[0].equals(desc)) {
				return stop;
			}
		}
		return null;
	}

	/**
	 * Read near by station
	 *
	 * @param position the position
	 * @return a list of station
	 */
	public final List<Station> readNearbyStation(final Position position) {

		final double dist = 0.004472;

		final List<Station> res = new ArrayList<>();
		final double latitude = position.getLatitude();
		final double longitude = position.getLongitude();

		final double latMax = latitude + dist;
		final double latMin = latitude - dist;
		final double lonMax = longitude + dist;
		final double lonMin = longitude - dist;

		for (final Station station : stationsOrderByName) {
			for (final Position stopPosition : station.getStopsPosition()) {
				final double trainLatitude = stopPosition.getLatitude();
				final double trainLongitude = stopPosition.getLongitude();
				if (trainLatitude <= latMax && trainLatitude >= latMin && trainLongitude <= lonMax && trainLongitude >= lonMin) {
					res.add(station);
					break;
				}
			}
		}
		return res;
	}

	public final List<Position> readPattern(final TrainLine line) {
		final List<Position> positions = new ArrayList<>();
		try {
			final CSVReader reader = new CSVReader(new InputStreamReader(ChicagoTracker.getContext().getAssets()
					.open("train_pattern/" + line.toTextString() + "_pattern.csv")));
			String[] row;
			while ((row = reader.readNext()) != null) {
				final double longitude = Double.valueOf(row[0]);
				final double latitude = Double.valueOf(row[1]);
				Position position = new Position();
				position.setLatitude(latitude);
				position.setLongitude(longitude);
				positions.add(position);
			}
			reader.close();
		} catch (final IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return positions;
	}

	/**
	 * Order stations
	 */
	private void order() {
		final List<Station> vals = new ArrayList<>();
		for (int i = 0; i < stations.size(); i++) {
			vals.add(stations.valueAt(i));
		}
		Collections.sort(vals);
		stationsOrderByName = new ArrayList<>();
		stationsOrderByLineMap = new TreeMap<>();
		for (final Station station : vals) {
			stationsOrderByName.add(station);
		}
		for (final Station station : vals) {
			final Set<TrainLine> tls = station.getLines();
			if (tls != null) {
				for (final TrainLine tl : tls) {
					List<Station> stations;
					if (stationsOrderByLineMap.containsKey(tl)) {
						stations = stationsOrderByLineMap.get(tl);
					} else {
						stations = new ArrayList<>();
						stationsOrderByLineMap.put(tl, stations);
					}
					stations.add(station);
					Collections.sort(stations);
				}
			}
		}
		stationsOrderByLine = new ArrayList<>();
		for (final Entry<TrainLine, List<Station>> e : stationsOrderByLineMap.entrySet()) {
			final List<Station> temp = e.getValue();
			stationsOrderByLine.addAll(temp);
		}
	}

	public SparseArray<Station> getStations() {
		return stations;
	}
}