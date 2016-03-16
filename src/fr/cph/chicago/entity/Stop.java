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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import lombok.Data;

/**
 * Station entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public class Stop implements Comparable<Stop>, Parcelable {
    /**
     * The id
     **/
    private int id;
    /**
     * The description
     **/
    private String description;
    /**
     * The train direction
     **/
    private TrainDirection direction;
    /**
     * The position
     **/
    private Position position;
    /**
     * Ada
     **/
    private boolean ada;
    /**
     * The list of train line
     **/
    private List<TrainLine> lines;

    /**
     *
     */
    public Stop() {
    }

    /**
     * @param in
     */
    private Stop(final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public final String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("[Id=").append(id);
        if (description != null) {
            stb.append(";description=").append(description);
        }
        if (direction != null) {
            stb.append(";direction=").append(direction);
        }
        if (position != null) {
            stb.append(";position=").append(position);
        }
        stb.append(";ada=").append(ada);
        if (lines != null) {
            stb.append(";lines=").append(lines);
        }
        stb.append("]");
        return stb.toString();
    }

    @Override
    public final int compareTo(@NonNull final Stop anotherStop) {
        return this.direction.compareTo(anotherStop.getDirection());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(description);
        dest.writeString(direction.toTextString());
        dest.writeParcelable(position, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeString(String.valueOf(ada));
        List<String> linesString = new ArrayList<>();
        for (TrainLine line : lines) {
            linesString.add(line.toTextString());
        }
        dest.writeStringList(linesString);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        description = in.readString();
        direction = TrainDirection.fromString(in.readString());
        position = in.readParcelable(Position.class.getClassLoader());
        ada = Boolean.valueOf(in.readString());
        List<String> linesString = new ArrayList<>();
        in.readStringList(linesString);
        lines = new ArrayList<>();
        for (String line : linesString) {
            lines.add(TrainLine.fromXmlString(line));
        }
    }

    public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };
}
