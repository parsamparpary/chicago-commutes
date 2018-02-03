/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindString
import butterknife.ButterKnife
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.entity.Position
import fr.cph.chicago.entity.Train
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.marker.RefreshTrainMarkers
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.rx.TrainEtaObserver
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainMapActivity : AbstractMapActivity() {

    @BindString(R.string.bundle_train_line)
    lateinit var bundleTrainLine: String
    @BindString(R.string.analytics_train_map)
    lateinit var analyticsTrainMap: String
    @BindString(R.string.request_runnumber)
    lateinit var requestRunNumber: String
    @BindString(R.string.request_rt)
    lateinit var requestRt: String

    private val trainService: TrainService
    private val observableUtil: ObservableUtil

    private var views: MutableMap<Marker, View>? = null
    private var line: String? = null
    private var status: MutableMap<Marker, Boolean>? = null
    private var markers: MutableList<Marker>? = null
    private var refreshTrainMarkers: RefreshTrainMarkers? = null

    private var centerMap = true
    private var drawLine = true

    init {
        this.views = HashMap()
        trainService = TrainService
        observableUtil = ObservableUtil
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.checkTrainData(this)
        if (!this.isFinishing) {
            MapsInitializer.initialize(applicationContext)
            setContentView(R.layout.activity_map)
            ButterKnife.bind(this)

            line = if (savedInstanceState != null)
                savedInstanceState.getString(bundleTrainLine)
            else
                intent.extras!!.getString(bundleTrainLine)

            // Init data
            initData()

            // Init toolbar
            setToolbar()

            // Google analytics
            Util.trackScreen(analyticsTrainMap)
        }
    }

    override fun initData() {
        super.initData()
        markers = ArrayList()
        status = HashMap()
        refreshTrainMarkers = RefreshTrainMarkers()
    }

    override fun setToolbar() {
        super.setToolbar()
        toolbar.setOnMenuItemClickListener { _ ->
            centerMap = false
            loadActivityData()
            false
        }

        val trainLine = TrainLine.fromXmlString(line!!)
        Util.setWindowsColor(this, toolbar, trainLine)
        toolbar.title = trainLine.toStringWithLine()
    }

    public override fun onStop() {
        super.onStop()
        centerMap = false
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        line = savedInstanceState.getString(bundleTrainLine)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState!!.putString(bundleTrainLine, line)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun centerMapOnTrain(result: List<Train>) {
        val position: Position
        val zoom: Int
        if (result.size == 1) {
            position = result[0].position
            zoom = 15
        } else {
            position = Train.getBestPosition(result)
            zoom = 11
        }
        centerMapOn(position.latitude, position.longitude, zoom)
    }

    private fun drawTrains(trains: List<Train>) {
        // TODO see if views can actually be null.
        if (views == null) {
            views = HashMap()
        } else {
            views!!.clear()
        }
        cleanAllMarkers()
        val bitmapDesc = refreshTrainMarkers!!.currentDescriptor
        trains.forEach { (routeNumber, destName, _, position, heading) ->
            val point = LatLng(position.latitude, position.longitude)
            val title = "To " + destName
            val snippet = Integer.toString(routeNumber)

            val marker = googleMap.addMarker(MarkerOptions().position(point).title(title).snippet(snippet).icon(bitmapDesc).anchor(0.5f, 0.5f).rotation(heading.toFloat()).flat(true))
            markers!!.add(marker)

            val view = layoutInflater.inflate(R.layout.marker, viewGroup, false)
            val title2 = view.findViewById<TextView>(R.id.title)
            title2.text = title

            views!![marker] = view
        }
    }

    private fun cleanAllMarkers() {
        markers!!.forEach({ it.remove() })
        markers!!.clear()
    }

    private fun drawLine(positions: List<Position>) {
        val poly = PolylineOptions()
        poly.width((application as App).lineWidth)
        poly.geodesic(true).color(TrainLine.fromXmlString(line!!).color)
        positions
            .map { position -> LatLng(position.latitude, position.longitude) }
            .forEach({ poly.add(it) })

        googleMap.addPolyline(poly)
        drawLine = false
    }

    override fun onCameraIdle() {
        refreshTrainMarkers!!.refresh(googleMap.cameraPosition, markers!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        googleMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                if ("" != marker.snippet) {
                    // View can be null
                    val view = views!![marker]
                    if (!refreshingInfoWindow) {
                        selectedMarker = marker
                        val runNumber = marker.snippet
                        observableUtil.createLoadTrainEtaObservable(runNumber, false)
                            .subscribe(TrainEtaObserver(view!!, this@TrainMapActivity))
                        status!![marker] = false
                    }
                    return view
                } else {
                    return null
                }
            }
        })
        googleMap.setOnInfoWindowClickListener { marker ->
            if ("" != marker.snippet) {
                val view = views!![marker]
                if (!refreshingInfoWindow) {
                    selectedMarker = marker
                    val runNumber = marker.snippet
                    val current = status!![marker]
                    observableUtil.createLoadTrainEtaObservable(runNumber, !current!!)
                        .subscribe(TrainEtaObserver(view!!, this@TrainMapActivity))
                    status!![marker] = !current
                }
            }
        }
        loadActivityData()
    }

    private fun loadActivityData() {
        if (Util.isNetworkAvailable()) {
            // Load train location
            val trainsObservable = observableUtil.createTrainLocationObservable(line!!)
            // Load pattern from local file
            val positionsObservable = observableUtil.createTrainPatternObservable(line!!)

            if (drawLine) {
                Observable.zip(trainsObservable, positionsObservable, BiFunction { trains: List<Train>, positions: List<Position> ->
                    // FIXME
                    if (trains != null) {
                        drawTrains(trains)
                        drawLine(positions)
                        if (trains.isNotEmpty()) {
                            if (centerMap) {
                                centerMapOnTrain(trains)
                            }
                        } else {
                            Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                        }
                    } else {
                        Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                    }
                    Any()
                }).subscribe()
            } else {
                trainsObservable.subscribe { trains ->
                    if (trains != null) {
                        drawTrains(trains)
                        if (trains.isEmpty()) {
                            Util.showMessage(this@TrainMapActivity, R.string.message_no_train_found)
                        }
                    } else {
                        Util.showMessage(this@TrainMapActivity, R.string.message_error_while_loading_data)
                    }
                }
            }
        } else {
            Util.showNetworkErrorMessage(layout)
        }
    }
}
