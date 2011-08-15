package org.mapsforge.applications.android.advancedmapviewer.routing;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.Vertex;

/**
 * A decision point is a point on a route where the user has to change streets.
 */
public class DecisionPoint {
	/**
	 * A human readable name for DecisionPoint (in most cases a street name)
	 */
	private String name;

	/**
	 * The position of the DecisionPoint
	 */
	private GeoPoint geoPoint;

	private int distance;

	/**
	 * Constructs a DecisionPint out of a name and a GeoPoint
	 * 
	 * @param name
	 *            human readable name string
	 * @param geoPoint
	 *            position in form of a GeoPoint
	 */
	public DecisionPoint(String name, GeoPoint geoPoint) {
		this.name = name;
		this.geoPoint = geoPoint;
		// this.distance = 0;
	}

	/**
	 * Constructs a DecisionPint out of a name and a Vertex
	 * 
	 * @param name
	 *            human readable name string
	 * @param vertex
	 *            position in form of a Vertex
	 */
	public DecisionPoint(String name, Vertex vertex) {
		this.name = name;
		vertex.getCoordinate();
		this.geoPoint = new GeoPoint(vertex.getCoordinate().getLatitude(), vertex
				.getCoordinate().getLongitude());
	}

	@Override
	public String toString() {
		return this.name + " (" + this.distance + "m)";
	}

	/**
	 * Getter for the position
	 * 
	 * @return position in form of a GeoPoint
	 */
	public GeoPoint getGeoPoint() {
		return this.geoPoint;
	}

	/**
	 * Getter for the name
	 * 
	 * @return human readable name
	 */
	public String getName() {
		return this.name;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}
