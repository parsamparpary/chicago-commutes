/**
 * Copyright 2019 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.activity.station

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindString
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener
import fr.cph.chicago.core.listener.GoogleMapOnClickListener
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.rx.BikeAllBikeStationsObserver
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Color
import fr.cph.chicago.util.Util

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BikeStationActivity : StationActivity(R.layout.activity_bike_station) {

    @BindView(R.id.activity_station_swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.activity_favorite_star)
    lateinit var favoritesImage: ImageView
    @BindView(R.id.activity_bike_station_streetview_image)
    lateinit var streetViewImage: ImageView
    @BindView(R.id.activity_bike_station_steetview_text)
    lateinit var streetViewText: TextView
    @BindView(R.id.activity_map_image)
    lateinit var mapImage: ImageView
    @BindView(R.id.map_container)
    lateinit var mapContainer: LinearLayout
    @BindView(R.id.walk_container)
    lateinit var walkContainer: LinearLayout
    @BindView(R.id.favorites_container)
    lateinit var favoritesImageContainer: LinearLayout
    @BindView(R.id.activity_bike_station_value)
    lateinit var bikeStationValue: TextView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.activity_bike_available_bike_value)
    lateinit var availableBikes: TextView
    @BindView(R.id.activity_bike_available_docks_value)
    lateinit var availableDocks: TextView

    @BindString(R.string.bundle_bike_station)
    lateinit var bundleBikeStation: String

    private val observableUtil: ObservableUtil = ObservableUtil
    private val preferenceService: PreferenceService = PreferenceService

    private lateinit var divvyStation: BikeStation
    private var isFavorite: Boolean = false

    override fun create(savedInstanceState: Bundle?) {
        divvyStation = intent.extras?.getParcelable(bundleBikeStation)
            ?: BikeStation.buildUnknownStation()
        val latitude = divvyStation.latitude
        val longitude = divvyStation.longitude

        swipeRefreshLayout.setOnRefreshListener {
            observableUtil.createAllBikeStationsObservable()
                .subscribe(BikeAllBikeStationsObserver(this, divvyStation.id, swipeRefreshLayout))
        }

        isFavorite = isFavorite()

        // Call google street api to load image
        loadGoogleStreetImage(Position(latitude, longitude), streetViewImage, streetViewText)

        if (isFavorite) {
            favoritesImage.setColorFilter(Color.yellowLineDark)
        }

        favoritesImageContainer.setOnClickListener { switchFavorite() }
        bikeStationValue.text = divvyStation.address
        streetViewImage.setOnClickListener(GoogleStreetOnClickListener(latitude, longitude))
        mapContainer.setOnClickListener(GoogleMapOnClickListener(latitude, longitude))
        walkContainer.setOnClickListener(GoogleMapDirectionOnClickListener(latitude, longitude))

        drawData()
        setToolBar()
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener {
            swipeRefreshLayout.isRefreshing = true
            observableUtil.createAllBikeStationsObservable()
                .subscribe(BikeAllBikeStationsObserver(this@BikeStationActivity, divvyStation.id, swipeRefreshLayout))
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = divvyStation.name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { finish() }
    }

    private fun drawData() {
        if (divvyStation.availableBikes == -1) {
            availableBikes.text = "?"
            availableBikes.setTextColor(Color.orange)
        } else {
            availableBikes.text = Util.formatBikesDocksValues(divvyStation.availableBikes)
            val color = if (divvyStation.availableBikes == 0) Color.red else Color.green
            availableBikes.setTextColor(color)
        }
        if (divvyStation.availableDocks == -1) {
            availableDocks.text = "?"
            availableDocks.setTextColor(Color.orange)
        } else {
            availableDocks.text = Util.formatBikesDocksValues(divvyStation.availableDocks)
            val color = if (divvyStation.availableDocks == 0) Color.red else Color.green
            availableDocks.setTextColor(color)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        divvyStation = savedInstanceState.getParcelable(bundleBikeStation)
            ?: BikeStation.buildUnknownStation()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable(bundleBikeStation, divvyStation)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Is favorite or not ?
     *
     * @return if the train station is favorite
     */
    override fun isFavorite(): Boolean {
        return preferenceService.isBikeStationFavorite(divvyStation.id)
    }

    fun refreshStation(station: BikeStation) {
        this.divvyStation = station
        drawData()
    }

    /**
     * Add/remove favorites
     */
    private fun switchFavorite() {
        isFavorite = if (isFavorite) {
            preferenceService.removeFromBikeFavorites(divvyStation.id, swipeRefreshLayout)
            favoritesImage.colorFilter = mapImage.colorFilter
            false
        } else {
            preferenceService.addToBikeFavorites(divvyStation.id, swipeRefreshLayout)
            preferenceService.addBikeRouteNameMapping(divvyStation.id.toString(), divvyStation.name)
            favoritesImage.setColorFilter(Color.yellowLineDark)
            App.instance.refresh = true
            true
        }
    }
}
