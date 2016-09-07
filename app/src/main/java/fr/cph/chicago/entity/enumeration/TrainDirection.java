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

package fr.cph.chicago.entity.enumeration;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

/**
 * Enumeration, train direction
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum TrainDirection {

    NORTH("N", "North"), SOUTH("S", "South"), EAST("E", "East"), WEST("W", "West"), UNKNOWN("U", "Unknown");

    /**
     * The text
     **/
    private final String text;
    /**
     * The formatted text
     **/
    private final String formattedText;

    /**
     * Private constructor
     *
     * @param text          the text
     * @param formattedText the formatted text
     */
    TrainDirection(final String text, final String formattedText) {
        this.text = text;
        this.formattedText = formattedText;
    }

    /**
     * Get train direction from string
     *
     * @param text the text
     * @return the train direction
     */
    @NonNull
    public static TrainDirection fromString(@NonNull final String text) {
        return Stream.of(TrainDirection.values())
            .filter(trainDirection -> text.equalsIgnoreCase(trainDirection.text))
            .findFirst()
            .orElse(TrainDirection.UNKNOWN);
    }

    @Override
    public final String toString() {
        return this.formattedText;
    }

    @NonNull
    public final String toTextString() {
        return this.text;
    }
}
