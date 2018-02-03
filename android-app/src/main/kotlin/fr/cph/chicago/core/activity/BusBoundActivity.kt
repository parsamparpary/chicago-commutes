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

import android.app.ListActivity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import butterknife.BindDrawable
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import fr.cph.chicago.Constants.Companion.BUSES_PATTERN_URL
import fr.cph.chicago.Constants.Companion.BUSES_STOP_URL
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.adapter.BusBoundAdapter
import fr.cph.chicago.entity.BusPattern
import fr.cph.chicago.entity.BusStop
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils

/**
 * Activity that represents the bus bound activity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusBoundActivity : ListActivity() {

    @BindView(R.id.bellow)
    lateinit var layout: LinearLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.bus_filter)
    lateinit var filter: EditText

    @BindString(R.string.bundle_bus_stop_id)
    lateinit var bundleBusStopId: String
    @BindString(R.string.bundle_bus_route_id)
    lateinit var bundleBusRouteId: String
    @BindString(R.string.bundle_bus_bound)
    lateinit var bundleBusBound: String
    @BindString(R.string.bundle_bus_bound_title)
    lateinit var bundleBusBoundTitle: String
    @BindString(R.string.bundle_bus_stop_name)
    lateinit var bundleBusStopName: String
    @BindString(R.string.bundle_bus_route_name)
    lateinit var bundleBusRouteName: String
    @BindString(R.string.bundle_bus_latitude)
    lateinit var bundleBusLatitude: String
    @BindString(R.string.bundle_bus_longitude)
    lateinit var bundleBusLongitude: String

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    lateinit var arrowBackWhite: Drawable

    private val observableUtil: ObservableUtil = ObservableUtil
    private val util: Util = Util

    private var mapFragment: MapFragment? = null
    private var busRouteId: String? = null
    private var busRouteName: String? = null
    private var bound: String? = null
    private var boundTitle: String? = null
    private var busBoundAdapter: BusBoundAdapter? = null
    private var busStops: List<BusStop>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.checkBusData(this)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_bus_bound)
            ButterKnife.bind(this)

            if (busRouteId == null || busRouteName == null || bound == null || boundTitle == null) {
                val extras = intent.extras
                busRouteId = extras!!.getString(bundleBusRouteId)
                busRouteName = extras.getString(bundleBusRouteName)
                bound = extras.getString(bundleBusBound)
                boundTitle = extras.getString(bundleBusBoundTitle)
            }
            busBoundAdapter = BusBoundAdapter()
            listAdapter = busBoundAdapter
            listView.setOnItemClickListener { _, _, position, _ ->
                val busStop = busBoundAdapter!!.getItem(position) as BusStop
                val intent = Intent(applicationContext, BusActivity::class.java)

                val extras = Bundle()
                extras.putInt(bundleBusStopId, busStop.id)
                extras.putString(bundleBusStopName, busStop.name)
                extras.putString(bundleBusRouteId, busRouteId)
                extras.putString(bundleBusRouteName, busRouteName)
                extras.putString(bundleBusBound, bound)
                extras.putString(bundleBusBoundTitle, boundTitle)
                extras.putDouble(bundleBusLatitude, busStop.position!!.latitude)
                extras.putDouble(bundleBusLongitude, busStop.position!!.longitude)

                intent.putExtras(extras)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            filter.addTextChangedListener(object : TextWatcher {
                private var busStopsFiltered: MutableList<BusStop>? = null

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    busStopsFiltered = mutableListOf()
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (busStops != null) {
                        busStops!!
                            .filter { busStop -> StringUtils.containsIgnoreCase(busStop.name, s) }
                            .forEach({ busStopsFiltered!!.add(it) })
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    busBoundAdapter!!.busStops = busStopsFiltered!!.toList()
                    busBoundAdapter!!.notifyDataSetChanged()
                }
            })


            util.setWindowsColor(this, toolbar, TrainLine.NA)
            toolbar.title = busRouteId + " - " + boundTitle

            toolbar.navigationIcon = arrowBackWhite
            toolbar.setOnClickListener { _ -> finish() }

            observableUtil.createBusStopBoundObservable(busRouteId!!, bound!!)
                .subscribe({ onNext ->
                    busStops = onNext
                    busBoundAdapter!!.busStops = onNext
                    busBoundAdapter!!.notifyDataSetChanged()
                }
                ) { onError ->
                    Log.e(TAG, onError.message, onError)
                    util.showOopsSomethingWentWrong(listView)
                }

            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_STOP_URL)

            // Preventing keyboard from moving background when showing up
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    public override fun onStart() {
        super.onStart()
        if (mapFragment == null) {
            val fm = fragmentManager
            val options = GoogleMapOptions()
            val camera = CameraPosition(util.chicago, 7f, 0f, 0f)
            options.camera(camera)
            mapFragment = MapFragment.newInstance(options)
            mapFragment!!.retainInstance = true
            fm.beginTransaction().replace(R.id.map, mapFragment).commit()
        }
    }

    public override fun onResume() {
        super.onResume()
        mapFragment!!.getMapAsync { googleMap ->
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isZoomControlsEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false
            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_PATTERN_URL)
            observableUtil.createBusPatternObservable(busRouteId!!, bound!!)
                .subscribe(
                    { busPattern ->
                        if (busPattern.direction != "error") {
                            val center = busPattern.points.size / 2
                            val position = busPattern.points[center].position
                            if (position.latitude == 0.0 && position.longitude == 0.0) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(util.chicago, 10f))
                            } else {
                                val latLng = LatLng(position.latitude, position.longitude)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(9f), 500, null)
                            }
                            drawPattern(busPattern)
                        } else {
                            util.showMessage(this, R.string.message_error_could_not_load_path)
                        }
                    }
                ) { onError ->
                    util.handleConnectOrParserException(onError, null, layout, layout)
                    Log.e(TAG, onError.message, onError)
                }
        }
    }

    private fun drawPattern(pattern: BusPattern) {
        mapFragment!!.getMapAsync { googleMap ->
            val poly = PolylineOptions()
            poly.geodesic(true).color(Color.BLACK)
            poly.width((application as App).lineWidth)
            pattern.points
                .map { patternPoint -> LatLng(patternPoint.position.latitude, patternPoint.position.longitude) }
                .forEach({ poly.add(it) })
            googleMap.addPolyline(poly)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        busRouteId = savedInstanceState.getString(bundleBusRouteId)
        busRouteName = savedInstanceState.getString(bundleBusRouteName)
        bound = savedInstanceState.getString(bundleBusBound)
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(bundleBusRouteId, busRouteId)
        savedInstanceState.putString(bundleBusRouteName, busRouteName)
        savedInstanceState.putString(bundleBusBound, bound)
        savedInstanceState.putString(bundleBusBoundTitle, boundTitle)
        super.onSaveInstanceState(savedInstanceState)
    }

    companion object {

        private val TAG = BusBoundActivity::class.java.simpleName
    }
}
