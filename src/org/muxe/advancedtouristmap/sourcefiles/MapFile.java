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
package org.muxe.advancedtouristmap.sourcefiles;

import java.io.File;

import org.mapsforge.android.maps.MapDatabase;

public class MapFile extends SourceFile {
	private double min_lat;
	private double max_lat;
	private double min_lon;
	private double max_lon;

	public double getMin_lat() {
		return this.min_lat;
	}

	public void setMin_lat(double min_lat) {
		this.min_lat = min_lat;
	}

	public double getMax_lat() {
		return this.max_lat;
	}

	public void setMax_lat(double max_lat) {
		this.max_lat = max_lat;
	}

	public double getMin_lon() {
		return this.min_lon;
	}

	public void setMin_lon(double min_lon) {
		this.min_lon = min_lon;
	}

	public double getMax_lon() {
		return this.max_lon;
	}

	public void setMax_lon(double max_lon) {
		this.max_lon = max_lon;
	}

	@Override
	public SourceFileType getType() {
		return SourceFileType.MAP;
	}

	@Override
	public boolean isValid(String basepath, boolean checkMD5) {
		return super.isValid(basepath, checkMD5)
				&& MapDatabase.isValidMapFile(basepath + File.separator
						+ this.getRelativePath());
	}
}
