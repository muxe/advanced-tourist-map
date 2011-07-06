package org.mapsforge.applications.android.advancedmapviewer.routing;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.Vertex;

public class DecisionPoint {
	private String name;
	private GeoPoint geoPoint;

	public DecisionPoint(String name, GeoPoint geoPoint) {
		this.name = name;
		this.geoPoint = geoPoint;
	}

	public DecisionPoint(String name, Vertex vertex) {
		this.name = name;
		vertex.getCoordinate();
		this.geoPoint = new GeoPoint(vertex.getCoordinate().getLatitude(), vertex
				.getCoordinate().getLongitude());
	}

	@Override
	public String toString() {
		return this.name;
	}

	public GeoPoint getGeoPoint() {
		return this.geoPoint;
	}

	public String getName() {
		return this.name;
	}
}
