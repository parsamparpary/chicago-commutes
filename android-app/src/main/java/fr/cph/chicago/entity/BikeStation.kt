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

package fr.cph.chicago.entity

import android.os.Parcel
import android.os.Parcelable
import com.annimon.stream.Collectors
import com.annimon.stream.Stream
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Bike station entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class BikeStation : Parcelable, AStation {

    @JsonProperty("id")
    var id: Int = 0
    @JsonProperty("stationName")
    var name: String? = null
    @JsonProperty("availableDocks")
    var availableDocks: Int? = null
    @JsonProperty("totalDocks")
    var totalDocks: Int? = null
    @JsonProperty("latitude")
    var latitude: Double = 0.toDouble()
    @JsonProperty("longitude")
    var longitude: Double = 0.toDouble()
    @JsonProperty("availableBikes")
    var availableBikes: Int? = null
    @JsonProperty("stAddress1")
    var stAddress1: String? = null

    constructor() {}

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeInt(availableDocks!!)
        dest.writeInt(totalDocks!!)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeInt(availableBikes!!)
        dest.writeString(stAddress1)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readInt()
        name = `in`.readString()
        availableDocks = `in`.readInt()
        totalDocks = `in`.readInt()
        latitude = `in`.readDouble()
        longitude = `in`.readDouble()
        availableBikes = `in`.readInt()
        stAddress1 = `in`.readString()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (javaClass != obj.javaClass)
            return false
        val other = obj as BikeStation?
        return id == other!!.id
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        result = id
        result = 31 * result + if (name != null) name!!.hashCode() else 0
        result = 31 * result + availableDocks!!
        result = 31 * result + totalDocks!!
        temp = java.lang.Double.doubleToLongBits(latitude)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(longitude)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        result = 31 * result + availableBikes!!
        result = 31 * result + if (stAddress1 != null) stAddress1!!.hashCode() else 0
        return result
    }

    companion object {

        private val DEFAULT_RANGE = 0.008

        fun readNearbyStation(bikeStations: List<BikeStation>, position: Position): List<BikeStation> {
            val latitude = position.latitude
            val longitude = position.longitude

            val latMax = latitude + DEFAULT_RANGE
            val latMin = latitude - DEFAULT_RANGE
            val lonMax = longitude + DEFAULT_RANGE
            val lonMin = longitude - DEFAULT_RANGE

            return Stream.of(bikeStations)
                .filter { station -> station.latitude <= latMax }
                .filter { station -> station.latitude >= latMin }
                .filter { station -> station.longitude <= lonMax }
                .filter { station -> station.longitude >= lonMin }
                .collect(Collectors.toList())
        }

        val CREATOR: Parcelable.Creator<BikeStation> = object : Parcelable.Creator<BikeStation> {
            override fun createFromParcel(`in`: Parcel): BikeStation {
                return BikeStation(`in`)
            }

            override fun newArray(size: Int): Array<BikeStation> {
                // FIXME parcelable kotlin
                return arrayOf()
            }
        }
    }
}
