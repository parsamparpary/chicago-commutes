package fr.cph.chicago.task;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.maps.SupportMapFragment;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.util.GPSAccess;
import fr.cph.chicago.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carl on 3/6/2016.
 */
public class LoadNearbyTask extends AsyncTask<Void, Void, Void> implements LocationListener {

	/**
	 * The position
	 **/
	private Position position;
	/**
	 * The list of bus stops
	 **/
	private List<BusStop> busStops;
	/**
	 * The list of train stations
	 **/
	private List<Station> trainStations;
	/**
	 * List of bike stations
	 **/
	private List<BikeStation> bikeStations;
	/**
	 * The location manager
	 **/
	private LocationManager locationManager;

	private MainActivity activity;

	private NearbyFragment fragment;

	private SupportMapFragment mapFragment;

	public LoadNearbyTask(final NearbyFragment fragment, final MainActivity activity, final SupportMapFragment mapFragment) {
		this.fragment = fragment;
		this.activity = activity;
		this.mapFragment = mapFragment;
		this.busStops = new ArrayList<>();
		this.trainStations = new ArrayList<>();
	}

	@Override
	protected final Void doInBackground(final Void... params) {
		bikeStations = activity.getIntent().getExtras().getParcelableArrayList(activity.getString(R.string.bundle_bike_stations));

		final DataHolder dataHolder = DataHolder.getInstance();
		final BusData busData = dataHolder.getBusData();
		final TrainData trainData = dataHolder.getTrainData();

		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		final GPSAccess gpsAccess = new GPSAccess(this, activity, locationManager);
		position = gpsAccess.getLocation();
		if (position != null) {
			busStops = busData.readNearbyStops(position);
			trainStations = trainData.readNearbyStation(position);
			// TODO: wait bikeStations is loaded
			if (bikeStations != null) {
				bikeStations = BikeStation.readNearbyStation(bikeStations, position);
			}
		}
		return null;
	}

	@Override
	protected final void onPostExecute(final Void result) {
		fragment.loadArrivals(busStops, trainStations, bikeStations);

		Util.centerMap(fragment, mapFragment, activity, position);

		if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
			return;
		}
		locationManager.removeUpdates(this);
	}

	@Override
	public final void onLocationChanged(final Location location) {
	}

	@Override
	public final void onProviderDisabled(final String provider) {
	}

	@Override
	public final void onProviderEnabled(final String provider) {
	}

	@Override
	public final void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}
}