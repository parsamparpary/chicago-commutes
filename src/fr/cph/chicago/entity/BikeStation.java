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

package fr.cph.chicago.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Bike station entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public final class BikeStation implements Parcelable {
    /**
     * Station id
     **/
    @JsonProperty("id")
    private int id;
    /**
     * Station name
     **/
    @JsonProperty("stationName")
    private String name;
    /**
     * Available docks
     **/
    @JsonProperty("availableDocks")
    private Integer availableDocks;
    /**
     * Total docks
     **/
    @JsonProperty("totalDocks")
    private Integer totalDocks;
    /**
     * The latitude
     **/
    @JsonProperty("latitude")
    private double latitude;
    /**
     * The longitude
     **/
    @JsonProperty("longitude")
    private double longitude;
    /**
     * Status value
     **/
    @JsonProperty("statusValue")
    private String statusValue;
    /**
     * Status key
     **/
    @JsonProperty("statusKey")
    private String statusKey;
    /**
     * Available bikes
     **/
    @JsonProperty("availableBikes")
    private Integer availableBikes;
    /**
     * Street address 1
     **/
    @JsonProperty("stAddress1")
    private String stAddress1;
    /**
     * Street address 2
     **/
    @JsonProperty("stAddress2")
    private String stAddress2;
    /**
     * City
     **/
    @JsonProperty("city")
    private String city;
    /**
     * Postal code
     **/
    @JsonProperty("postalCode")
    private String postalCode;
    /**
     * Location
     **/
    @JsonProperty("location")
    private String location;
    /**
     * Altitude
     **/
    @JsonProperty("altitude")
    private String altitude;
    /**
     * Test station
     **/
    @JsonProperty("testStation")
    private boolean testStation;
    /**
     * Last communication time
     **/
    @JsonProperty("lastCommunicationTime")
    private String lastCommunicationTime;
    /**
     * Land mark
     **/
    @JsonProperty("landMark")
    private int landMark;

    public BikeStation() {

    }

    /**
     * @param in
     */
    private BikeStation(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public final String toString() {
        return "[" + id + " " + name + " " + availableBikes + "/" + totalDocks + "]";
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(availableDocks);
        dest.writeInt(totalDocks);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(statusValue);
        dest.writeString(statusKey);
        dest.writeInt(availableBikes);
        dest.writeString(stAddress1);
        dest.writeString(stAddress2);
        dest.writeString(city);
        dest.writeString(postalCode);
        dest.writeString(location);
        dest.writeString(altitude);
        dest.writeString(String.valueOf(testStation));
        dest.writeString(lastCommunicationTime);
        dest.writeInt(landMark);
    }

    private void readFromParcel(final Parcel in) {
        id = in.readInt();
        name = in.readString();
        availableDocks = in.readInt();
        totalDocks = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        statusValue = in.readString();
        statusKey = in.readString();
        availableBikes = in.readInt();
        stAddress1 = in.readString();
        stAddress2 = in.readString();
        city = in.readString();
        postalCode = in.readString();
        location = in.readString();
        altitude = in.readString();
        testStation = Boolean.valueOf(in.readString());
        lastCommunicationTime = in.readString();
        landMark = in.readInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BikeStation other = (BikeStation) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + availableDocks;
        result = 31 * result + totalDocks;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (statusValue != null ? statusValue.hashCode() : 0);
        result = 31 * result + (statusKey != null ? statusKey.hashCode() : 0);
        result = 31 * result + availableBikes;
        result = 31 * result + (stAddress1 != null ? stAddress1.hashCode() : 0);
        result = 31 * result + (stAddress2 != null ? stAddress2.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        result = 31 * result + (testStation ? 1 : 0);
        result = 31 * result + (lastCommunicationTime != null ? lastCommunicationTime.hashCode() : 0);
        result = 31 * result + landMark;
        return result;
    }

    public static final Parcelable.Creator<BikeStation> CREATOR = new Parcelable.Creator<BikeStation>() {
        public BikeStation createFromParcel(Parcel in) {
            return new BikeStation(in);
        }

        public BikeStation[] newArray(int size) {
            return new BikeStation[size];
        }
    };

    public static List<BikeStation> readNearbyStation(final List<BikeStation> bikeStations, final Position position) {
        final double dist = 0.004472;
        final double latitude = position.getLatitude();
        final double longitude = position.getLongitude();

        final double latMax = latitude + dist;
        final double latMin = latitude - dist;
        final double lonMax = longitude + dist;
        final double lonMin = longitude - dist;

        final List<BikeStation> bikeStationsRes = new ArrayList<>();
        for (final BikeStation station : bikeStations) {
            final double bikeLatitude = station.getLatitude();
            final double bikeLongitude = station.getLongitude();
            if (bikeLatitude <= latMax && bikeLatitude >= latMin && bikeLongitude <= lonMax && bikeLongitude >= lonMin) {
                bikeStationsRes.add(station);
            }
        }
        return bikeStationsRes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
