package org.mapsforge.applications.android.advancedmapviewer.sourcefiles;

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
