/*
 * Copyright 2011 mapsforge.org
 *
 *	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.muxe.advancedtouristmap.poi;

import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;

public class PoiOrCategory {
	private PointOfInterest poi;
	private PoiCategory poiCat;

	public PoiOrCategory(PointOfInterest poi) {
		this.poi = poi;
	}

	public PoiOrCategory(PoiCategory poiCat) {
		this.poiCat = poiCat;
	}

	public boolean isPoi() {
		return this.poi != null;
	}

	public boolean isPoiCategory() {
		return this.poiCat != null;
	}

	public PoiCategory getPoiCategory() {
		return this.poiCat;
	}

	public PointOfInterest getPoi() {
		return this.poi;
	}
}
