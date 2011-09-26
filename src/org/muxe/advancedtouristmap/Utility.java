package org.muxe.advancedtouristmap;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.GeoCoordinate;

public class Utility {
	
	/**
	 * Converts a GeoCoordinate into a GeoPoint
	 * @param gc the GeoCoordinate
	 * @return a GeoPoint representing the GeoCoordinate
	 */
	public static GeoPoint geoCoordinateToGeoPoint(GeoCoordinate gc) {
		return new GeoPoint(gc.getLatitude(), gc.getLongitude());
	}
}
