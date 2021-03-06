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

package fr.cph.chicago.service

import android.util.Log
import fr.cph.chicago.R
import fr.cph.chicago.client.CtaClient
import fr.cph.chicago.client.CtaRequestType.BUS_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.BUS_DIRECTION
import fr.cph.chicago.client.CtaRequestType.BUS_PATTERN
import fr.cph.chicago.client.CtaRequestType.BUS_ROUTES
import fr.cph.chicago.client.CtaRequestType.BUS_STOP_LIST
import fr.cph.chicago.client.CtaRequestType.BUS_VEHICLES
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Bus
import fr.cph.chicago.core.model.BusArrival
import fr.cph.chicago.core.model.BusDirections
import fr.cph.chicago.core.model.BusPattern
import fr.cph.chicago.core.model.BusRoute
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.BusStopPattern
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.dto.BusArrivalStopDTO
import fr.cph.chicago.core.model.enumeration.BusDirection
import fr.cph.chicago.entity.BusArrivalResponse
import fr.cph.chicago.entity.BusDirectionResponse
import fr.cph.chicago.entity.BusPatternResponse
import fr.cph.chicago.entity.BusPositionResponse
import fr.cph.chicago.entity.BusRoutesResponse
import fr.cph.chicago.entity.BusStopsResponse
import fr.cph.chicago.exception.CtaException
import fr.cph.chicago.parser.BusStopCsvParser
import fr.cph.chicago.repository.BusRepository
import fr.cph.chicago.util.Util
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import java.text.SimpleDateFormat
import java.util.Locale

object BusService {

    private val TAG = BusService::class.java.simpleName
    private val busStopCsvParser = BusStopCsvParser
    private val preferenceService = PreferenceService
    private val busRepository = BusRepository
    private val ctaClient = CtaClient
    private val util = Util
    private val simpleDateFormatBus: SimpleDateFormat = SimpleDateFormat("yyyyMMdd HH:mm", Locale.US)

    fun loadFavoritesBuses(): List<BusArrival> {
        val favoritesBusParams = preferenceService.getFavoritesBusParams()
        if (favoritesBusParams.isEmpty) return mutableListOf()
        val params = ArrayListValuedHashMap<String, String>(2, 1)
        val routeIdParam = App.instance.getString(R.string.request_rt)
        val stopIdParam = App.instance.getString(R.string.request_stop_id)
        params.put(routeIdParam, favoritesBusParams.get(routeIdParam).joinToString(separator = ","))
        params.put(stopIdParam, favoritesBusParams.get(stopIdParam).joinToString(separator = ","))
        return getBusArrivals(params)
    }

    fun loadOneBusStop(stopId: String, bound: String): List<BusStop> {
        val params = ArrayListValuedHashMap<String, String>(2, 1)
        params.put(App.instance.getString(R.string.request_rt), stopId)
        params.put(App.instance.getString(R.string.request_dir), bound)
        val result = ctaClient.get(BUS_STOP_LIST, params, BusStopsResponse::class.java)
        if (result.bustimeResponse.stops == null) {
            throw CtaException(result)
        }
        return result.bustimeResponse.stops!!.map { stop ->
            BusStop(
                id = stop.stpid.toInt(),
                name = stop.stpnm,
                description = stop.stpnm,
                position = Position(stop.lat, stop.lon)
            )
        }
    }

    fun loadLocalBusData(): Any {
        if (busRepository.hasBusStopsEmpty()) {
            Log.d(TAG, "Load bus stop from CSV")
            busStopCsvParser.parse()
        }
        return Any()
    }

    fun loadBusDirections(busRouteId: String): BusDirections {
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put(App.instance.getString(R.string.request_rt), busRouteId)
        val result = ctaClient.get(BUS_DIRECTION, params, BusDirectionResponse::class.java)
        if (result.bustimeResponse.directions == null) {
            throw CtaException(result)
        }
        val busDirections = BusDirections(busRouteId)
        result
            .bustimeResponse
            .directions
            .map { direction -> BusDirection.fromString(direction.dir) }
            .forEach { busDirections.addBusDirection(it) }
        return busDirections
    }

    fun loadBusRoutes(): List<BusRoute> {
        return ctaClient.get(BUS_ROUTES, ArrayListValuedHashMap<String, String>(), BusRoutesResponse::class.java)
            .bustimeResponse
            .routes
            .map { route -> BusRoute(route.routeId, route.routeName) }
    }

    fun loadFollowBus(busId: String): List<BusArrival> {
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put(App.instance.getString(R.string.request_vid), busId)
        return getBusArrivals(params)
    }

    fun loadBusPattern(busRouteId: String, bound: String): BusPattern {
        return loadBusPattern(busRouteId, arrayOf(bound))[0]
    }

    fun loadBusPattern(busRouteId: String, bounds: Array<String>): List<BusPattern> {
        val connectParam = ArrayListValuedHashMap<String, String>(1, 1)
        connectParam.put(App.instance.getString(R.string.request_rt), busRouteId)
        val boundIgnoreCase = bounds.map { bound -> bound.toLowerCase(Locale.US) }
        val result = ctaClient.get(BUS_PATTERN, connectParam, BusPatternResponse::class.java)
        if (result.bustimeResponse.ptr == null) throw CtaException(result)
        return result
            .bustimeResponse
            .ptr!!
            .map { ptr ->
                BusPattern(
                    direction = ptr.rtdir,
                    busStopsPatterns = ptr.pt
                        .map { pt -> BusStopPattern(Position(pt.lat, pt.lon), pt.typ, pt.stpnm ?: "") }
                        .toMutableList()
                )
            }
            .filter { pattern ->
                val directionIgnoreCase = pattern.direction.toLowerCase(Locale.US)
                boundIgnoreCase.contains(directionIgnoreCase)
            }
    }

    fun loadBus(busRouteId: String): List<Bus> {
        val connectParam = ArrayListValuedHashMap<String, String>(1, 1)
        connectParam.put(App.instance.getString(R.string.request_rt), busRouteId)
        val result = ctaClient.get(BUS_VEHICLES, connectParam, BusPositionResponse::class.java)
        if (result.bustimeResponse.vehicle == null) throw CtaException(result)
        return result.bustimeResponse.vehicle!!
            .map { vehicle ->
                val position = Position(vehicle.lat.toDouble(), vehicle.lon.toDouble())
                Bus(vehicle.vid.toInt(), position, vehicle.hdg.toInt(), vehicle.des)
            }
    }

    fun loadAroundBusArrivals(busStop: BusStop): List<BusArrival> {
        val busStopId = busStop.id
        val params = ArrayListValuedHashMap<String, String>(1, 1)
        params.put(App.instance.getString(R.string.request_stop_id), busStopId.toString())
        return getBusArrivals(params)
    }

    fun getBusStopsAround(position: Position): List<BusStop> {
        return busRepository.getBusStopsAround(position)
    }

    fun saveBusStops(busStops: List<BusStop>) {
        return busRepository.saveBusStops(busStops)
    }

    fun getBusRoutes(): List<BusRoute> {
        return busRepository.inMemoryBusRoutes
    }

    fun saveBusRoutes(busRoutes: List<BusRoute>) {
        busRepository.saveBusRoutes(busRoutes)
    }

    /**
     *  We can't guaranty that the repo will be populated when we call that method
     */
    fun getBusRoute(routeId: String): BusRoute {
        return if (busRepository.isEmpty()) {
            getBusRouteFromFavorites(routeId)
        } else {
            val busRoute = busRepository.getBusRoute(routeId)
            if (busRoute.name != "error") {
                busRoute
            } else {
                getBusRouteFromFavorites(routeId)
            }
        }
    }

    private fun getBusRouteFromFavorites(routeId: String): BusRoute {
        val routeName = preferenceService.getBusRouteNameMapping(routeId)
        return BusRoute(routeId, routeName ?: "")
    }

    fun busRouteError(): Boolean {
        return busRepository.busRouteError
    }

    fun setBusRouteError(value: Boolean) {
        busRepository.busRouteError = value
    }

    fun searchBusRoutes(query: String): List<BusRoute> {
        return getBusRoutes()
            .filter { (id, name) -> containsIgnoreCase(id, query) || containsIgnoreCase(name, query) }
            .distinct()
            .sortedWith(util.busStopComparatorByName)
    }

    fun loadBusArrivals(requestRt: String, busRouteId: String, requestStopId: String, busStopId: Int, bound: String, boundTitle: String): BusArrivalStopDTO {
        val params = ArrayListValuedHashMap<String, String>(2, 1)
        params.put(requestRt, busRouteId)
        params.put(requestStopId, busStopId.toString())
        return getBusArrivals(params)
            .filter { (_, _, _, _, _, _, routeDirection) -> routeDirection == bound || routeDirection == boundTitle }
            .fold(BusArrivalStopDTO()) { accumulator, busArrival ->
                if (accumulator.containsKey(busArrival.busDestination)) {
                    (accumulator[busArrival.busDestination] as MutableList).add(busArrival)
                } else {
                    accumulator.put(busArrival.busDestination, mutableListOf(busArrival))
                }
                accumulator
            }
    }

    private fun getBusArrivals(params: MultiValuedMap<String, String>): List<BusArrival> {
        val result = ctaClient.get(BUS_ARRIVALS, params, BusArrivalResponse::class.java)
        if (result.bustimeResponse.prd == null) {
            if (result.bustimeResponse.error != null && result.bustimeResponse.error!!.isNotEmpty()) {
                if (result.bustimeResponse.error!![0].noServiceScheduled()) {
                    return listOf()
                }
            }
            throw CtaException(result)
        }
        val buses = result.bustimeResponse
            .prd!!
            .map { prd ->
                BusArrival(
                    timeStamp = simpleDateFormatBus.parse(prd.tmstmp),
                    errorMessage = StringUtils.EMPTY, // TODO evaluate why there is this field
                    stopName = prd.stpnm,
                    stopId = prd.stpid.toInt(),
                    busId = prd.vid.toInt(),
                    routeId = prd.rt,
                    routeDirection = BusDirection.fromString(prd.rtdir).text,
                    busDestination = prd.des,
                    predictionTime = simpleDateFormatBus.parse(prd.prdtm),
                    isDelay = prd.dly)
            }
        // limiting the number of bus arrival returned so it's not too ugly on the map
        return if (buses.size >= 20) buses.subList(0, 19) else buses
    }
}
