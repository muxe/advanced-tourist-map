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

import java.util.ArrayList;

/**
 * Contains all information about one Map Bundle
 * 
 * @author Max Dörfler
 */
public class MapBundle {
	private String filepathXml;
	private String name;
	private MapFile mapFile;
	private AddressFile addressFile;
	private PoiFile poiFile;
	private ArrayList<RoutingFile> routingFiles;

	// private PoiFile poiFile;
	// private AddressFile addressFile;

	public MapBundle() {
		this.routingFiles = new ArrayList<RoutingFile>();
	}

	public void setFilepathXml(String filenpathXml) {
		this.filepathXml = filenpathXml;
	}

	public String getFilepathXml() {
		return this.filepathXml;
	}

	public MapFile getMapFile() {
		return this.mapFile;
	}

	public void setMapFile(MapFile mapFile) {
		this.mapFile = mapFile;
	}

	public void addRoutingFile(RoutingFile rf) {
		this.routingFiles.add(rf);
	}

	public ArrayList<RoutingFile> getRoutingFiles() {
		return this.routingFiles;
	}

	public RoutingFile[] getRoutingFilesArray() {
		if (this.getRoutingFiles() == null) {
			return new RoutingFile[0];
		}
		RoutingFile[] rfs = this.getRoutingFiles().toArray(
				new RoutingFile[this.getRoutingFiles().size()]);
		return rfs;
	}

	public boolean isValid() {
		// TODO: implement
		return this.mapFile != null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public AddressFile getAddressFile() {
		return this.addressFile;
	}

	public void setAddressFile(AddressFile addressFile) {
		this.addressFile = addressFile;
	}

	public PoiFile getPoiFile() {
		return this.poiFile;
	}

	public void setPoiFile(PoiFile poiFile) {
		this.poiFile = poiFile;
	}

	public boolean isRoutable() {
		return this.getRoutingFiles().size() > 0;
	}

	public boolean isSearchable() {
		return this.getAddressFile() != null;
	}

	public boolean isPoiable() {
		return this.getPoiFile() != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MapBundle other = (MapBundle) obj;
		if (!this.filepathXml.equals(other.filepathXml)) {
			return false;
		}
		return true;
	}
}
