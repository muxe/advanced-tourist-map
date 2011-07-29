package org.mapsforge.applications.android.advancedmapviewer.sourcefiles;

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
}
