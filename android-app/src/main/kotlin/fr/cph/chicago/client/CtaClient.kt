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

package fr.cph.chicago.client

import fr.cph.chicago.Constants.Companion.ALERTS_ROUTES_URL
import fr.cph.chicago.Constants.Companion.ALERT_ROUTES_URL
import fr.cph.chicago.Constants.Companion.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.Companion.BUSES_DIRECTION_URL
import fr.cph.chicago.Constants.Companion.BUSES_PATTERN_URL
import fr.cph.chicago.Constants.Companion.BUSES_ROUTES_URL
import fr.cph.chicago.Constants.Companion.BUSES_STOP_URL
import fr.cph.chicago.Constants.Companion.BUSES_VEHICLES_URL
import fr.cph.chicago.Constants.Companion.TRAINS_ARRIVALS_URL
import fr.cph.chicago.Constants.Companion.TRAINS_FOLLOW_URL
import fr.cph.chicago.Constants.Companion.TRAINS_LOCATION_URL
import fr.cph.chicago.client.CtaRequestType.ALERTS_ROUTE
import fr.cph.chicago.client.CtaRequestType.ALERTS_ROUTES
import fr.cph.chicago.client.CtaRequestType.BUS_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.BUS_DIRECTION
import fr.cph.chicago.client.CtaRequestType.BUS_PATTERN
import fr.cph.chicago.client.CtaRequestType.BUS_ROUTES
import fr.cph.chicago.client.CtaRequestType.BUS_STOP_LIST
import fr.cph.chicago.client.CtaRequestType.BUS_VEHICLES
import fr.cph.chicago.client.CtaRequestType.TRAIN_ARRIVALS
import fr.cph.chicago.client.CtaRequestType.TRAIN_FOLLOW
import fr.cph.chicago.client.CtaRequestType.TRAIN_LOCATION
import fr.cph.chicago.core.App
import fr.cph.chicago.parser.JsonParser
import org.apache.commons.collections4.MultiValuedMap

/**
 * Class that build url and connect to CTA API
 *
 * @author Carl-Philipp Harmant
 *
 * @version 1
 */
object CtaClient {

    private const val QUERY_PARAM_KEY = "?key="
    private const val QUERY_PARAM_JSON_ALERT = "?outputType=json"

    private val jsonParser = JsonParser

    fun <T> get(requestType: CtaRequestType, params: MultiValuedMap<String, String>, clazz: Class<T>): T {
        val address = when (requestType) {
            TRAIN_ARRIVALS -> TRAINS_ARRIVALS_URL + QUERY_PARAM_KEY + App.ctaTrainKey + "&outputType=JSON"
            TRAIN_FOLLOW -> TRAINS_FOLLOW_URL + QUERY_PARAM_KEY + App.ctaTrainKey + "&outputType=JSON"
            TRAIN_LOCATION -> TRAINS_LOCATION_URL + QUERY_PARAM_KEY + App.ctaTrainKey + "&outputType=JSON"
            BUS_ROUTES -> BUSES_ROUTES_URL + QUERY_PARAM_KEY + App.ctaBusKey + "&format=json"
            BUS_DIRECTION -> BUSES_DIRECTION_URL + QUERY_PARAM_KEY + App.ctaBusKey + "&format=json"
            BUS_STOP_LIST -> BUSES_STOP_URL + QUERY_PARAM_KEY + App.ctaBusKey + "&format=json"
            BUS_VEHICLES -> BUSES_VEHICLES_URL + QUERY_PARAM_KEY + App.ctaBusKey + "&format=json"
            BUS_ARRIVALS -> BUSES_ARRIVAL_URL + QUERY_PARAM_KEY + App.ctaBusKey + "&format=json"
            BUS_PATTERN -> BUSES_PATTERN_URL + QUERY_PARAM_KEY + App.ctaBusKey + "&format=json"
            ALERTS_ROUTES -> ALERTS_ROUTES_URL + QUERY_PARAM_JSON_ALERT
            ALERTS_ROUTE -> ALERT_ROUTES_URL + QUERY_PARAM_JSON_ALERT
            else -> throw RuntimeException("Unknown request type")
        }
        val res = address + params.asMap()
            .flatMap { entry -> entry.value.map { value -> "&${entry.key}=$value" } }
            .joinToString(separator = "")

        val inputStream = HttpClient.connect(res)
        return jsonParser.parse(inputStream, clazz)
    }
}
