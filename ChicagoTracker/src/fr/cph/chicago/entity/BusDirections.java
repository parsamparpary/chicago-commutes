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

package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.entity.enumeration.BusDirection;

/**
 * Bus directions entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BusDirections {
	/** The id **/
	private String id;
	/** List of bus direction **/
	private List<BusDirection> lBusDirection;

	/**
	 * Constructor
	 */
	public BusDirections() {
		lBusDirection = new ArrayList<BusDirection>();
	}

	/**
	 * 
	 * @return
	 */
	public final String getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 */
	public final void setId(final String id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
	 */
	public final List<BusDirection> getlBusDirection() {
		return lBusDirection;
	}

	/**
	 * 
	 * @param lBusDirection
	 */
	public final void setlBusDirection(final List<BusDirection> lBusDirection) {
		this.lBusDirection = lBusDirection;
	}

	/**
	 * 
	 * @param dir
	 */
	public final void addBusDirection(final BusDirection dir) {
		if (!lBusDirection.contains(dir)) {
			lBusDirection.add(dir);
		}
	}

}
