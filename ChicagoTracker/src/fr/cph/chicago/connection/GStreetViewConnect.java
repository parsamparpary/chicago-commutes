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

package fr.cph.chicago.connection;

import android.graphics.drawable.Drawable;
import android.util.Log;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.util.Util;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Class that access google street api. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class GStreetViewConnect {

	private static final String TAG = GStreetViewConnect.class.getSimpleName();
	private static final String BASE_URL = "http://maps.googleapis.com/maps/api/streetview";
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 300;

	private String googleKey;

	private static GStreetViewConnect instance = null;

	/**
	 * Private constructor, that get the API key from property file
	 */
	private GStreetViewConnect() {
		googleKey = Util.getProperty("google.streetmap.key");
	}

	/**
	 * Get instance of this class
	 *
	 * @return
	 */
	public static GStreetViewConnect getInstance() {
		if (instance == null) {
			instance = new GStreetViewConnect();
		}
		return instance;
	}

	/**
	 * Build Url
	 *
	 * @param position position that we want to access
	 * @return a drawable
	 * @throws IOException an exception
	 */
	public final Drawable connect(final Position position) throws IOException {
		final StringBuilder address = new StringBuilder(BASE_URL);
		address.append("?key=");
		address.append(googleKey);
		address.append("&sensor=false");
		address.append("&size=" + WIDTH + "x" + HEIGHT);
		address.append("&fov=120");
		address.append("&location=");
		address.append(position.getLatitude());
		address.append(",").append(position.getLongitude());
		return connectUrl(address.toString());
	}

	/**
	 * Connect to the API and get the MAP
	 *
	 * @param address the address to connect to
	 * @return a drawable map
	 * @throws IOException
	 */
	private Drawable connectUrl(final String address) {
		Log.v(TAG, "Address: " + address);
		InputStream is = null;
		try {
			is = (InputStream) new URL(address).getContent();
			return Drawable.createFromStream(is, "src name");
		} catch (final Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}