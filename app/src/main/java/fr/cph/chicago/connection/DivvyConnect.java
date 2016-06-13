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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.cph.chicago.R;
import fr.cph.chicago.exception.ConnectException;

public class DivvyConnect {

	private static final String TAG = DivvyConnect.class.getSimpleName();

	private static DivvyConnect instance = null;

	private DivvyConnect() {
	}

    @NonNull
	public static DivvyConnect getInstance() {
		if (instance == null) {
			instance = new DivvyConnect();
		}
		return instance;
	}

    @NonNull
	public final InputStream connect(@NonNull final Context context) throws ConnectException {
		final InputStream inputStream;
		try {
            final String urlDivvy = context.getString(R.string.url_divvy);
            Log.v(TAG, "Address: " + urlDivvy);
			final URL url = new URL(urlDivvy);
			final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setReadTimeout(5000);
			inputStream = new BufferedInputStream(urlConnection.getInputStream());
		} catch (final IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new ConnectException(ConnectException.ERROR, e);
		}
		return inputStream;
	}
}