package org.mapsforge.applications.android.advancedmapviewer.routing;

public class RoutingFile {

	public int id;
	public String name;
	public String description;
	public String path;

	public RoutingFile(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public boolean store() {
		return true;
	}

	public boolean delete() {
		// TODO: implement
		return true;
	}

	/**
	 * Checks if the RoutingFile is valid. e.g. if the file (still) exists
	 * 
	 * @return boolean if the RoutingFile is valid
	 */
	public boolean isValid() {
		// TODO: implement
		return true;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
