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

package fr.cph.chicago.core.listener

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.BusStopActivity
import fr.cph.chicago.core.adapter.PopupBusDetailsFavoritesAdapter
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.dto.BusDetailsDTO
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.Util
import io.reactivex.Observable

class BusStopOnClickListener(private val activity: Activity, private val parent: ViewGroup, private val busDetailsDTOs: List<BusDetailsDTO>) : View.OnClickListener {

    override fun onClick(view: View) {
        if (busDetailsDTOs.size == 1) {
            val busDetails = busDetailsDTOs[0]
            loadBusDetails(view, busDetails)
        } else {
            val ada = PopupBusDetailsFavoritesAdapter(activity, busDetailsDTOs)
            val vi = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = vi.inflate(R.layout.popup_bus, parent, false)
            val listView: ListView = popupView.findViewById(R.id.details)
            listView.adapter = ada
            val builder = AlertDialog.Builder(activity)
            builder.setAdapter(ada) { _, position ->
                val busDetails = busDetailsDTOs[position]
                loadBusDetails(view, busDetails)
            }
            val dialog = builder.create()
            dialog.show()
            if (dialog.window != null) {
                dialog.window?.setLayout(((activity.application as App).screenWidth * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun loadBusDetails(view: View, busDetails: BusDetailsDTO) {
        observableUtil.createBusStopBoundObservable(busDetails.busRouteId, busDetails.boundTitle)
            .subscribe({ onNext ->
                Observable.fromIterable(onNext)
                    .filter { busStop -> busStop.id.toString() == busDetails.stopId }
                    .firstElement()
                    .subscribe({ busStop: BusStop ->
                        val intent = Intent(activity, BusStopActivity::class.java)
                        val extras = Bundle()
                        extras.putInt(activity.getString(R.string.bundle_bus_stop_id), busStop.id)
                        extras.putString(activity.getString(R.string.bundle_bus_stop_name), busStop.name)
                        extras.putString(activity.getString(R.string.bundle_bus_route_id), busDetails.busRouteId)
                        extras.putString(activity.getString(R.string.bundle_bus_route_name), busDetails.routeName)
                        extras.putString(activity.getString(R.string.bundle_bus_bound), busDetails.bound)
                        extras.putString(activity.getString(R.string.bundle_bus_bound_title), busDetails.boundTitle)
                        extras.putDouble(activity.getString(R.string.bundle_bus_latitude), busStop.position.latitude)
                        extras.putDouble(activity.getString(R.string.bundle_bus_longitude), busStop.position.longitude)

                        intent.putExtras(extras)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        activity.startActivity(intent)
                    }
                    ) { onError ->
                        Log.e(TAG, onError.message, onError)
                        util.showOopsSomethingWentWrong(parent)
                    }
            }
            ) { onError ->
                Log.e(TAG, onError.message, onError)
                util.showNetworkErrorMessage(view)
            }
    }

    companion object {
        private val TAG = BusStopOnClickListener::class.java.simpleName
        private val util = Util
        private val observableUtil = ObservableUtil
    }
}
