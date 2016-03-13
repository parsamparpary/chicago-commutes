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

package fr.cph.chicago.activity;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.DivvyConnect;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.fragment.BikeFragment;
import fr.cph.chicago.fragment.BusFragment;
import fr.cph.chicago.fragment.FavoritesFragment;
import fr.cph.chicago.fragment.NearbyFragment;
import fr.cph.chicago.fragment.TrainFragment;
import fr.cph.chicago.json.JsonParser;
import fr.cph.chicago.task.GlobalConnectTask;
import fr.cph.chicago.util.Util;
import org.apache.commons.collections4.MultiValuedMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.cph.chicago.connection.CtaRequestType.BUS_ARRIVALS;
import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String SELECTED_ID = "SELECTED_ID";
	private static final int POSITION_BUS = 2;

	private int currentPosition;

	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;

	private FavoritesFragment favoritesFragment;
	private TrainFragment trainFragment;
	private BusFragment busFragment;
	private BikeFragment bikeFragment;
	private NearbyFragment nearbyFragment;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			if (savedInstanceState != null) {
				ChicagoTracker.reloadData();
			}
			setContentView(R.layout.activity_main);

			new LoadBusAndBikeData().execute();

			ChicagoTracker.container = (FrameLayout) findViewById(R.id.container);
			ChicagoTracker.container.getForeground().setAlpha(0);

			initView();
			setToolbar();

			drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			mDrawerLayout.addDrawerListener(drawerToggle);
			drawerToggle.syncState();

			currentPosition = savedInstanceState == null ? R.id.navigation_favorites : savedInstanceState.getInt(SELECTED_ID);
			itemSelection(currentPosition);
		}
	}

	@Override
	public void onBackPressed() {
		if (currentPosition == R.id.navigation_favorites) {
			finish();
		} else {
			itemSelection(R.id.navigation_favorites);
		}
	}

	private void initView() {
		final NavigationView mDrawer = (NavigationView) findViewById(R.id.main_drawer);
		mDrawer.setNavigationItemSelectedListener(this);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	}

	private void setToolbar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(final MenuItem item) {
					if (toolbar.getTitle().equals(getString(R.string.nearby))) {
						nearbyFragment.reloadData();
					} else {
						// Favorite fragment
						if (favoritesFragment != null) {
							favoritesFragment.startRefreshing();
						}

						Util.loadFavorites(favoritesFragment, FavoritesFragment.class, MainActivity.this);

						// Google analytics
						Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_arrivals, 0);
						Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_arrival, 0);
						Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
						// Check if bus/bike or alert data are not loaded. If not, load them.
						// Can happen when the app has been loaded without any data connection
						boolean loadData = false;
						final DataHolder dataHolder = DataHolder.getInstance();

						final BusData busData = dataHolder.getBusData();

						final Bundle bundle = MainActivity.this.getIntent().getExtras();
						final List<BikeStation> bikeStations = bundle.getParcelableArrayList("bikeStations");

						if (busData.getRoutes() != null && busData.getRoutes().size() == 0) {
							loadData = true;
						}
						if (!loadData && bikeStations == null) {
							loadData = true;
						}
						if (loadData) {
							favoritesFragment.startRefreshing();
							new LoadBusAndBikeData().execute();
						}
						Util.trackAction(MainActivity.this, R.string.analytics_category_ui, R.string.analytics_action_press, R.string.analytics_action_refresh_fav, 0);
					}
					return true;
				}
			});
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				toolbar.setElevation(4);
			}
			toolbar.inflateMenu(R.menu.main);
		}
	}

	private void itemSelection(final int position) {
		final FragmentManager fragmentManager = getSupportFragmentManager();
		currentPosition = position;
		CharSequence title = null;
		switch (position) {
		case R.id.navigation_favorites:
			title = getString(R.string.favorites);
			if (favoritesFragment == null) {
				favoritesFragment = FavoritesFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, favoritesFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			showActionBarMenu();
			break;
		case R.id.navigation_train:
			title = getString(R.string.train);
			if (trainFragment == null) {
				trainFragment = TrainFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, trainFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			hideActionBarMenu();
			break;
		case R.id.navigation_bus:
			title = getString(R.string.bus);
			if (busFragment == null) {
				busFragment = BusFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, busFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			hideActionBarMenu();
			break;
		case R.id.navigation_bike:
			title = getString(R.string.divvy);
			if (bikeFragment == null) {
				bikeFragment = BikeFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, bikeFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			hideActionBarMenu();
			break;
		case R.id.navigation_nearby:
			title = getString(R.string.nearby);
			if (nearbyFragment == null) {
				nearbyFragment = NearbyFragment.newInstance(position + 1);
			}
			if (!this.isFinishing()) {
				fragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).commit();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
			showActionBarMenu();
			break;
		}
		if (title != null) {
			toolbar.setTitle(title);
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onNavigationItemSelected(final MenuItem menuItem) {
		menuItem.setChecked(true);
		currentPosition = menuItem.getItemId();
		itemSelection(currentPosition);
		return true;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState, final PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		//save selected item so it will remains same even after orientation change
		outState.putInt(SELECTED_ID, currentPosition);
	}

	private void hideActionBarMenu() {
		if (toolbar.getMenu().getItem(0).isVisible()) {
			showHideActionBarMenu(false);
		}
	}

	private void showActionBarMenu() {
		if (!toolbar.getMenu().getItem(0).isVisible()) {
			showHideActionBarMenu(true);
		}
	}

	private void showHideActionBarMenu(final boolean bool) {
		toolbar.getMenu().getItem(0).setVisible(bool);
	}

	public final class LoadBusAndBikeData extends AsyncTask<Void, Void, Void> {

		private BusData busData;
		private List<BikeStation> bikeStations;

		@Override
		protected final Void doInBackground(final Void... params) {
			final DataHolder dataHolder = DataHolder.getInstance();
			busData = dataHolder.getBusData();

			// Load buses data
			try {
				busData.loadBusRoutes();
				Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus, R.string.analytics_action_get_bus_routes, 0);
				publishProgress();
			} catch (final ParserException | ConnectException e) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "Bus error: " + e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
				Log.e(TAG, e.getMessage(), e);
			}

			// Load divvy data
			try {
				final JsonParser json = JsonParser.getInstance();
				final DivvyConnect divvyConnect = DivvyConnect.getInstance();
				final InputStream bikeContent = divvyConnect.connect();
				bikeStations = json.parseStations(bikeContent);
				Collections.sort(bikeStations, Util.BIKE_COMPARATOR_NAME);
				Util.trackAction(MainActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_divvy, R.string.analytics_action_get_divvy_all, 0);
				publishProgress();
			} catch (final ConnectException | ParserException e) {
				Log.e(TAG, e.getMessage(), e);
				bikeStations = new ArrayList<>();
			}
			return null;
		}

		@Override
		protected final void onProgressUpdate(final Void... progress) {
		}

		@Override
		protected final void onPostExecute(final Void result) {
			// Put data into data holder
			final DataHolder dataHolder = DataHolder.getInstance();
			dataHolder.setBusData(busData);

			getIntent().putParcelableArrayListExtra("bikeStations", (ArrayList<BikeStation>) bikeStations);
			onNewIntent(getIntent());
			if (favoritesFragment != null) {
				favoritesFragment.setBikeStations(bikeStations);
			}
			if (bikeFragment != null) {
				bikeFragment.setBikeStations(bikeStations);
			}
			if (currentPosition == POSITION_BUS && busFragment != null) {
				busFragment.update();
			}
		}
	}
}