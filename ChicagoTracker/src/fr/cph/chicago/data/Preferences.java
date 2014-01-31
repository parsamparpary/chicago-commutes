/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

public final class Preferences {

	/** Tag **/
	private static final String TAG = "Preferences";

	public static void saveBusFavorites(String name, List<String> favorites) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Set<String> set = new LinkedHashSet<String>();
		for (String fav : favorites) {
			set.add(fav);
		}
		Log.v(TAG, "Put bus favorites: " + favorites.toString());
		editor.putStringSet(name, set);
		editor.commit();
	}

	public static List<String> getBusFavorites(String name) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		Set<String> setPref = sharedPref.getStringSet(name, null);
		List<String> favorites = new ArrayList<String>();
		if (setPref != null) {
			Iterator<String> it = setPref.iterator();
			while (it.hasNext()) {
				String value = it.next();
				favorites.add(value);
			}
		}
		Collections.sort(favorites, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				String derp1 = Util.decodeBusFavorite(str1)[0];
				String derp2 = Util.decodeBusFavorite(str2)[0];
				Integer int1 = null;
				Integer int2 = null;
				try {
					int1 = Integer.valueOf(derp1);
				} catch (NumberFormatException e) {
					int1 = Integer.valueOf(derp1.substring(0, derp1.length() - 1));
				}
				try {
					int2 = Integer.valueOf(derp2);
				} catch (NumberFormatException e) {
					int2 = Integer.valueOf(derp2.substring(0, derp2.length() - 1));
				}
				return int1.compareTo(int2);
			}
		});
		Log.v(TAG, "Read bus favorites : " + favorites.toString());
		return favorites;
	}

	public static void saveTrainFavorites(String name, List<Integer> favorites) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Set<String> set = new LinkedHashSet<String>();
		for (Integer favorite : favorites) {
			set.add(favorite.toString());
		}
		Log.v(TAG, "Put train favorites: " + favorites.toString());
		editor.putStringSet(name, set);
		editor.commit();
	}

	public static List<Integer> getTrainFavorites(String name) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		Set<String> setPref = sharedPref.getStringSet(name, null);
		List<Integer> favorites = new ArrayList<Integer>();
		if (setPref != null) {
			Iterator<String> it = setPref.iterator();
			while (it.hasNext()) {
				String value = it.next();
				favorites.add(Integer.valueOf(value));
			}
		}
		DataHolder dataHolder = DataHolder.getInstance();
		List<Station> stations = new ArrayList<Station>();
		for (Integer favorite : favorites) {
			Station station = dataHolder.getTrainData().getStation(favorite);
			stations.add(station);
		}
		Collections.sort(stations);
		List<Integer> res = new ArrayList<Integer>();
		for (Station station : stations) {
			res.add(station.getId());
		}
		Log.v(TAG, "Read train favorites : " + res.toString());
		return res;
	}

	public static void saveTrainFilter(Integer stationId, TrainLine line, TrainDirection direction, boolean value) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, value);
		editor.commit();
	}

	public static boolean getTrainFilter(Integer stationId, TrainLine line, TrainDirection direction) {
		Context context = ChicagoTracker.getAppContext();
		SharedPreferences sharedPref = context.getSharedPreferences(ChicagoTracker.PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
		boolean result = sharedPref.getBoolean(String.valueOf(stationId) + "_" + line + "_" + direction, true);
		return result;
	}
}
