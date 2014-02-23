/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity.enumeration;

/**
 * Enumeration, prediction type
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum PredictionType {
	ARRIVAL("A"), DEPARTURE("D");

	/** The message **/
	private String message;

	/**
	 * Private constructor
	 * 
	 * @param message
	 */
	private PredictionType(String message) {
		this.message = message;
	}

	/**
	 * Get Prediction type from string
	 * 
	 * @param text
	 *            the text
	 * @return a prediction type
	 */
	public static final PredictionType fromString(final String text) {
		if (text != null) {
			for (PredictionType b : PredictionType.values()) {
				if (text.equalsIgnoreCase(b.message)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public final String toString() {
		return this.message;
	}
}
