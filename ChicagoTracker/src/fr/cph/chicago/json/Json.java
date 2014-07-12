package fr.cph.chicago.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;

public class Json {
	public List<BikeStation> parseStations(final String jsonString) throws ParserException {
		List<BikeStation> stations = new ArrayList<BikeStation>();
		try {
			JSONObject json = new JSONObject(jsonString);
			JSONArray stationList = json.getJSONArray("stationBeanList");
			for (int i = 0; i < stationList.length(); i++) {
				JSONObject jsonStation = stationList.getJSONObject(i);
				BikeStation station = new BikeStation();
				station.setId(jsonStation.getInt("id"));
				station.setName(jsonStation.getString("stationName"));
				station.setAvailableDocks(jsonStation.getInt("availableDocks"));
				station.setTotalDocks(jsonStation.getInt("totalDocks"));
				Position position = new Position();
				position.setLatitude(jsonStation.getDouble("latitude"));
				position.setLongitude(jsonStation.getDouble("longitude"));
				station.setPosition(position);
				station.setStatusValue(jsonStation.getString("statusValue"));
				station.setStatusKey(jsonStation.getString("statusKey"));
				station.setAvailableBikes(jsonStation.getInt("availableBikes"));
				station.setStAddress1(jsonStation.getString("stAddress1"));
				station.setStAddress2(jsonStation.getString("stAddress2"));
				station.setCity(jsonStation.getString("city"));
				station.setPostalCode(jsonStation.getString("postalCode"));
				station.setLocation(jsonStation.getString("location"));
				station.setAltitude(jsonStation.getString("altitude"));
				station.setTestStation(jsonStation.getBoolean("testStation"));
				station.setLastCommunicationTime(jsonStation.getString("lastCommunicationTime"));
				station.setLandMark(jsonStation.getInt("landMark"));
				stations.add(station);
			}
		} catch (JSONException e) {
			throw new ParserException(TrackerException.ERROR, e);
		}
		return stations;
	}
}